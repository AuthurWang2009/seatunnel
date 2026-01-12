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

import org.apache.seatunnel.common.constants.JobMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Config extends Serializable {

    String getString(String key);

    int getInt(String key);

    boolean getBoolean(String key);

    Config getConfig(String key);

    <T> T get(ConfigEntry<T> option);

    Object getValue(String key);

    <T> java.util.Optional<T> getOptional(ConfigEntry<T> option);

    Map<String, Object> toMap();

    Config withValue(String key, Object value);

    Config merge(Config other);

    boolean hasPath(String key);

    java.util.List<String> getStringList(String key);

    JobMode getEnum(Class<JobMode> jobModeClass, String key);

    List<Config> getConfigList(String key);

    default boolean isEmpty() {
        return toMap().isEmpty();
    }

    default Set<Map.Entry<String, Object>> entrySet() {
        return toMap().entrySet();
    }

    static Config of(Map<String, Object> map) {
        return ReadonlyConfig.fromMap(map);
    }

    /**
     * Extract the source configurations from the given config. If the source is a Config object,
     * convert each key to a Config with plugin_name attribute. If it's already a List<Config>,
     * return it directly.
     *
     * @param config the config to extract source from
     * @return the list of source configurations
     */
    static List<Config> getSource(Config config) {
        return extractConfigList(config, "source");
    }

    /**
     * Extract the transform configurations from the given config. If the transform is a Config
     * object, convert each key to a Config with plugin_name attribute. If it's already a
     * List<Config>, return it directly.
     *
     * @param config the config to extract transform from
     * @return the list of transform configurations
     */
    static List<Config> getTransform(Config config) {
        return extractConfigList(config, "transform");
    }

    /**
     * Extract the sink configurations from the given config. If the sink is a Config object,
     * convert each key to a Config with plugin_name attribute. If it's already a List<Config>,
     * return it directly.
     *
     * @param config the config to extract sink from
     * @return the list of sink configurations
     */
    static List<Config> getSink(Config config) {
        return extractConfigList(config, "sink");
    }

    /**
     * Helper method to extract config list from a given tag. If the tag value is a Config, convert
     * each key to a Config with plugin_name attribute. If the tag value is a List<Config>, return
     * it directly.
     *
     * @param config the parent config
     * @param tag the tag name (source, transform, or sink)
     * @return the list of configurations
     */
    static List<Config> extractConfigList(Config config, String tag) {
        if (!config.hasPath(tag)) {
            return new ArrayList<>();
        }

        Object value = config.getValue(tag);

        // If it's already a list, use getConfigList
        if (value instanceof List) {
            return config.getConfigList(tag);
        }

        // If it's a Config object, convert each key to a Config with plugin_name
        Config tagConfig = config.getConfig(tag);
        List<Config> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : tagConfig.entrySet()) {
            String pluginName = entry.getKey();
            Object pluginValue = entry.getValue();

            Config pluginConfig;
            if (pluginValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pluginMap = (Map<String, Object>) pluginValue;
                pluginConfig = Config.of(pluginMap);
            } else {
                pluginConfig = Config.of(new java.util.HashMap<>());
            }

            // Add plugin_name attribute
            pluginConfig = pluginConfig.withValue("plugin_name", pluginName);
            result.add(pluginConfig);
        }
        return result;
    }
}
