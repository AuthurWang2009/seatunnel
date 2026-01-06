/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.api.config;

import java.util.ArrayList;
import java.util.List;

public class JobConfigBuilder {

    private final ConfigBuilder envConfigBuilder;
    private final List<Config> sources;
    private final List<Config> transforms;
    private final List<Config> sinks;

    private JobConfigBuilder() {
        this.envConfigBuilder = ConfigBuilder.create();
        this.sources = new ArrayList<>();
        this.transforms = new ArrayList<>();
        this.sinks = new ArrayList<>();
    }

    public static JobConfigBuilder create() {
        return new JobConfigBuilder();
    }

    public JobConfigBuilder env(Config envConfig) {
        this.envConfigBuilder.config(envConfig);
        return this;
    }

    public JobConfigBuilder addEnv(String key, Object value) {
        this.envConfigBuilder.put(key, value);
        return this;
    }

    public JobConfigBuilder source(String pluginName, Config config) {
        Config sourceConfig = ConfigBuilder.of(config).put("plugin_name", pluginName).build();
        this.sources.add(sourceConfig);
        return this;
    }

    public JobConfigBuilder transform(String pluginName, Config config) {
        Config transformConfig = ConfigBuilder.of(config).put("plugin_name", pluginName).build();
        this.transforms.add(transformConfig);
        return this;
    }

    public JobConfigBuilder sink(String pluginName, Config config) {
        Config sinkConfig = ConfigBuilder.of(config).put("plugin_name", pluginName).build();
        this.sinks.add(sinkConfig);
        return this;
    }

    public Config build() {
        return ConfigBuilder.create()
                .put("env", envConfigBuilder.build().toMap())
                .put("source", convertConfigList(sources))
                .put("transform", convertConfigList(transforms))
                .put("sink", convertConfigList(sinks))
                .build();
    }

    private List<Object> convertConfigList(List<Config> configs) {
        List<Object> result = new ArrayList<>();
        for (Config config : configs) {
            // We use toSourceMap or similar to get raw object, but interface only allows toMap
            // (Map<String, String>) or cast.
            // Since we implemented ReadonlyConfig.getSourceMap(), we might need to rely on
            // implementation details or clean interface.
            // For now, let's assume we can use ReadonlyConfig or assume toMap() is enough if values
            // are simple, but for nested objects it might be tricky.
            // The safest is to rely on ConfigBuilder logic or cast if we know implementation.
            // Ideally Config interface should have a method to get raw map or value.
            // But we can reconstruct it. Or simply use the internal map if available.

            if (config instanceof ReadonlyConfig) {
                result.add(((ReadonlyConfig) config).getSourceMap());
            } else {
                // Fallback
                result.add(config.toMap());
            }
        }
        return result;
    }
}
