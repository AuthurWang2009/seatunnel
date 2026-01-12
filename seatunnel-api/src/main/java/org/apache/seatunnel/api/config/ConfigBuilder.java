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

import org.apache.seatunnel.api.config.util.ConfigShadeUtils;
import org.apache.seatunnel.api.sink.TablePlaceholder;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.common.utils.PlaceholderUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Used to build the {@link Config} from config file. */
@Slf4j
public class ConfigBuilder {

    private static final String PLACEHOLDER_REGEX = "\\$\\{([^:{}]+)(?::[^}]*)?\\}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);

    private final Map<String, Object> configMap = new LinkedHashMap<>();

    private ConfigBuilder() {}

    /**
     * Create a new ConfigBuilder instance.
     *
     * @return ConfigBuilder
     */
    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }

    /**
     * Create a new ConfigBuilder instance initialized with the given Config for modification
     *
     * @param config The initial config
     * @return ConfigBuilder
     */
    public static ConfigBuilder of(Config config) {
        ConfigBuilder builder = new ConfigBuilder();
        builder.from(config);
        return builder;
    }

    /**
     * Add a key-value pair to the config.
     *
     * @param key The key
     * @param value The value
     * @return this
     */
    public ConfigBuilder put(String key, Object value) {
        this.configMap.put(key, value);
        return this;
    }

    /**
     * Merge another Config into this builder.
     *
     * @param config The config to merge
     * @return this
     */
    public ConfigBuilder from(Config config) {
        if (config instanceof ReadonlyConfig) {
            this.configMap.putAll(((ReadonlyConfig) config).getSourceMap());
        } else {
            this.configMap.putAll(config.toMap());
        }
        return this;
    }

    /**
     * Build the Config object.
     *
     * @return The built Config
     */
    public Config build() {
        return Config.of(configMap);
    }

    // ===================================
    // Static Factory/Utility Methods
    // ===================================

    public static Config of(@NonNull String filePath) {
        return of(Paths.get(filePath));
    }

    public static Config of(@NonNull String filePath, List<String> variables) {
        return of(Paths.get(filePath), variables);
    }

    public static Config of(@NonNull Path filePath) {
        return of(filePath, null);
    }

    public static Config of(@NonNull Path filePath, List<String> variables) {
        log.info("Loading config file from path: {}", filePath);
        // Note: ConfigAdapter usage is deprecated/legacy. If needed, we can re-introduce using
        // service loader manually or moving ConfigAdapterUtils.
        // For now, we use ConfigLoader.
        return ofInner(filePath, variables);
    }

    public static String mapToString(Map<String, Object> configMap) {
        return JsonUtils.toPrettyJsonString(configMap);
    }

    public static List<String> extractPlaceholder(String input) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        List<String> placeholders = new ArrayList<>();

        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        return placeholders;
    }

    // ===================================
    // Private Helper Methods
    // ===================================

    private static Config ofInner(@NonNull Path filePath, List<String> variables) {
        // 1. Set variables to system properties
        if (variables != null) {
            variables.stream()
                    .filter(Objects::nonNull)
                    .map(variable -> variable.split("=", 2))
                    .filter(pair -> pair.length == 2)
                    .peek(
                            pair -> {
                                if (TablePlaceholder.isSystemPlaceholder(pair[0])) {
                                    throw new IllegalArgumentException(
                                            "System placeholders cannot be used. Incorrect config parameter: "
                                                    + pair[0]);
                                }
                            })
                    .forEach(pair -> System.setProperty(pair[0], pair[1]));
        }

        // 2. Load config via ConfigLoader
        // This will trigger SPI parser which should handle resolution using system properties
        Config config = ConfigLoader.load(filePath);

        // 3. Process manual variable substitution if any variables provided
        // This handles cases where SPI parser resolution might differ or specific replacement logic
        // is needed
        if (variables != null) {
            Map<String, Object> configMap = config.toMap();
            // Perform in-place substitution on the map
            processVariablesMap(configMap);
            // Updating config with substituted map
            config = ReadonlyConfig.fromMap(configMap);
        }

        return ConfigShadeUtils.decryptConfig(config);
    }

    private static void processVariablesMap(Map<String, Object> mapValue) {
        mapValue.forEach(
                (innerKey, innerValue) -> {
                    if (innerValue instanceof Map) {
                        processVariablesMap((Map<String, Object>) innerValue);
                    } else if (innerValue instanceof List) {
                        mapValue.put(innerKey, processVariablesList((List<?>) innerValue));
                    } else {
                        processSingleVariable(innerKey, innerValue, mapValue);
                    }
                });
    }

    private static List<?> processVariablesList(List<?> list) {
        return list.stream()
                .map(
                        variable -> {
                            if (variable instanceof String) {
                                return replacePlaceholders((String) variable);
                            } else if (variable instanceof Map) {
                                processVariablesMap((Map<String, Object>) variable);
                                return variable;
                            } else if (variable instanceof List) {
                                return processVariablesList((List<?>) variable);
                            }
                            return variable;
                        })
                .collect(Collectors.toList());
    }

    private static void processSingleVariable(
            String variableKey, Object variableValue, Map<String, Object> parentMap) {
        if (Objects.isNull(variableValue)) {
            return;
        }
        String variableString = variableValue.toString();
        String replacedValue = replacePlaceholders(variableString);
        if (!variableString.equals(replacedValue)) {
            parentMap.put(variableKey, replacedValue);
        }
    }

    private static String replacePlaceholders(String variableString) {
        List<String> placeholders = extractPlaceholder(variableString);
        String result = variableString;
        for (String placeholder : placeholders) {
            result =
                    PlaceholderUtils.replacePlaceholders(
                            result, placeholder, System.getProperty(placeholder), null);
        }
        return result;
    }
}
