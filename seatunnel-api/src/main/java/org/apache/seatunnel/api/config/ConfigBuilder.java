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

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigBuilder {

    private final Map<String, Object> configMap;

    private ConfigBuilder() {
        this.configMap = new LinkedHashMap<>();
    }

    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }

    public static ConfigBuilder of(Config config) {
        return create().config(config);
    }

    @SuppressWarnings("unchecked")
    public ConfigBuilder config(Config config) {
        if (config == null) {
            return this;
        }
        if (config instanceof ReadonlyConfig) {
            Map<String, Object> source = ((ReadonlyConfig) config).getSourceMap();
            deepMerge(this.configMap, source);
        } else {
            Map<String, String> keyValues = config.toMap();
            keyValues.forEach(this::put);
        }
        return this;
    }

    public ConfigBuilder put(String key, Object value) {
        putRecursive(this.configMap, key, value);
        return this;
    }

    public Config build() {
        return new ReadonlyConfig(this.configMap);
    }

    @SuppressWarnings("unchecked")
    private void putRecursive(Map<String, Object> map, String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = map;
        for (int i = 0; i < keys.length - 1; i++) {
            String k = keys[i];
            currentMap.computeIfAbsent(k, x -> new LinkedHashMap<String, Object>());
            Object v = currentMap.get(k);
            if (!(v instanceof Map)) {
                v = new LinkedHashMap<String, Object>();
                currentMap.put(k, v);
            }
            currentMap = (Map<String, Object>) v;
        }
        currentMap.put(keys[keys.length - 1], value);
    }

    @SuppressWarnings("unchecked")
    private void deepMerge(Map<String, Object> base, Map<String, Object> overlay) {
        for (Map.Entry<String, Object> entry : overlay.entrySet()) {
            if (entry.getValue() instanceof Map && base.get(entry.getKey()) instanceof Map) {
                deepMerge(
                        (Map<String, Object>) base.get(entry.getKey()),
                        (Map<String, Object>) entry.getValue());
            } else {
                base.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
