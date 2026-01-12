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

package org.apache.seatunnel.api.config.util;

import org.apache.seatunnel.api.config.Config;
import org.apache.seatunnel.api.config.ConfigShade;
import org.apache.seatunnel.common.Constants;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Config shade utilities */
@Slf4j
public final class ConfigShadeUtils {

    private static final String SHADE_IDENTIFIER_OPTION = "shade.identifier";
    private static final String SHADE_PROPS_OPTION = "shade.properties";
    private static final String SHADE_OPTIONS_OPTION = "shade.options";

    public static final String[] DEFAULT_SENSITIVE_KEYWORDS =
            new String[] {"password", "username", "auth", "token", "access_key", "secret_key"};

    private static final Map<String, ConfigShade> CONFIG_SHADES = new HashMap<>();

    private static final ConfigShade DEFAULT_SHADE = new DefaultConfigShade();

    static {
        ServiceLoader<ConfigShade> serviceLoader = ServiceLoader.load(ConfigShade.class);
        Iterator<ConfigShade> it = serviceLoader.iterator();
        it.forEachRemaining(
                configShade -> {
                    CONFIG_SHADES.put(configShade.getIdentifier(), configShade);
                });
        log.info("Load config shade spi: {}", CONFIG_SHADES.keySet());
    }

    private static class DefaultConfigShade implements ConfigShade {
        private static final String IDENTIFIER = "default";

        @Override
        public String getIdentifier() {
            return IDENTIFIER;
        }

        @Override
        public String encrypt(String content) {
            return content;
        }

        @Override
        public String decrypt(String content) {
            return content;
        }
    }

    public static String encryptOption(String identifier, String content) {
        ConfigShade configShade = CONFIG_SHADES.getOrDefault(identifier, DEFAULT_SHADE);
        return configShade.encrypt(content);
    }

    public static String decryptOption(String identifier, String content) {
        ConfigShade configShade = CONFIG_SHADES.getOrDefault(identifier, DEFAULT_SHADE);
        return configShade.decrypt(content);
    }

    public static Config decryptConfig(Config config) {
        String identifier =
                config.hasPath(Constants.ENV)
                                && config.getConfig(Constants.ENV).hasPath(SHADE_IDENTIFIER_OPTION)
                        ? config.getConfig(Constants.ENV).getString(SHADE_IDENTIFIER_OPTION)
                        : DEFAULT_SHADE.getIdentifier();

        Map<String, Object> props = new HashMap<>();
        if (config.hasPath(Constants.ENV)
                && config.getConfig(Constants.ENV).hasPath(SHADE_PROPS_OPTION)) {
            Config propsConfig = config.getConfig(Constants.ENV).getConfig(SHADE_PROPS_OPTION);
            props.putAll(propsConfig.toMap());
        }

        return decryptConfig(identifier, config, props);
    }

    public static Config encryptConfig(Config config) {
        String identifier =
                config.hasPath(Constants.ENV)
                                && config.getConfig(Constants.ENV).hasPath(SHADE_IDENTIFIER_OPTION)
                        ? config.getConfig(Constants.ENV).getString(SHADE_IDENTIFIER_OPTION)
                        : DEFAULT_SHADE.getIdentifier();

        Map<String, Object> props = new HashMap<>();
        if (config.hasPath(Constants.ENV)
                && config.getConfig(Constants.ENV).hasPath(SHADE_PROPS_OPTION)) {
            Config propsConfig = config.getConfig(Constants.ENV).getConfig(SHADE_PROPS_OPTION);
            props.putAll(propsConfig.toMap());
        }
        return encryptConfig(identifier, config, props);
    }

    private static Config decryptConfig(
            String identifier, Config config, Map<String, Object> props) {
        return processConfig(identifier, config, true, props);
    }

    private static Config encryptConfig(
            String identifier, Config config, Map<String, Object> props) {
        return processConfig(identifier, config, false, props);
    }

    @SuppressWarnings("unchecked")
    private static Config processConfig(
            String identifier, Config config, boolean isDecrypted, Map<String, Object> props) {
        ConfigShade configShade = CONFIG_SHADES.getOrDefault(identifier, DEFAULT_SHADE);
        // call open method before the encrypt/decrypt
        configShade.open(props);

        Set<String> sensitiveOptions = new HashSet<>(getSensitiveOptions(config));
        sensitiveOptions.addAll(Arrays.asList(configShade.sensitiveOptions()));
        BiFunction<String, Object, Object> processFunction =
                (key, value) -> {
                    if (value instanceof List) {
                        List<String> list = (List<String>) value;
                        List<String> processedList = new ArrayList<>();
                        for (String element : list) {
                            processedList.add(
                                    isDecrypted
                                            ? configShade.decrypt(element)
                                            : configShade.encrypt(element));
                        }
                        return processedList;
                    } else {
                        return isDecrypted
                                ? configShade.decrypt((String) value)
                                : configShade.encrypt((String) value);
                    }
                };

        // Use LinkedHashMap to preserve order if possible, though ReadonlyConfig toMap does this.
        Map<String, Object> configMap = new LinkedHashMap<>(config.toMap());

        // Deep parsing/handling might be needed if values are not Maps but generic objects?
        // ReadonlyConfig ensures structure.

        if (configMap.containsKey(Constants.SOURCE)) {
            Object sourceVal = configMap.get(Constants.SOURCE);
            if (sourceVal instanceof List) {
                List<Map<String, Object>> sources = (List<Map<String, Object>>) sourceVal;
                Preconditions.checkArgument(
                        !sources.isEmpty(), "Miss <Source> config! Please check the config file.");
                List<Map<String, Object>> newSources = new ArrayList<>();
                for (Map<String, Object> source : sources) {
                    Map<String, Object> newSource = new LinkedHashMap<>(source);
                    for (String sensitiveOption : sensitiveOptions) {
                        newSource.computeIfPresent(sensitiveOption, processFunction);
                    }
                    newSources.add(newSource);
                }
                configMap.put(Constants.SOURCE, newSources);
            } else if (sourceVal instanceof Map) {
                Map<String, Object> source = new LinkedHashMap<>((Map<String, Object>) sourceVal);
                // For single source (Map), we might need to iterate its values if it's {PluginName:
                // {options...}}
                // But typically sensitive options are inside the plugin block.
                // If structure is { MySQL-CDC: { ... } }, we need to go deeper?
                // Wait, if it's Map, it usually means ONE source.
                // Is the Sensitive Option at top level of source block or inside Plugin block?
                // Config structure: source { MySQL-CDC { password = ... } }
                // So source is a Map. Its values are Maps (Plugins).
                // We need to traverse values.
                for (Map.Entry<String, Object> entry : source.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> pluginConfig =
                                new LinkedHashMap<>((Map<String, Object>) entry.getValue());
                        for (String sensitiveOption : sensitiveOptions) {
                            pluginConfig.computeIfPresent(sensitiveOption, processFunction);
                        }
                        source.put(entry.getKey(), pluginConfig);
                    }
                }
                configMap.put(Constants.SOURCE, source);
            }
        }

        if (configMap.containsKey(Constants.SINK)) {
            Object sinkVal = configMap.get(Constants.SINK);
            if (sinkVal instanceof List) {
                List<Map<String, Object>> sinks = (List<Map<String, Object>>) sinkVal;
                Preconditions.checkArgument(
                        !sinks.isEmpty(), "Miss <Sink> config! Please check the config file.");
                List<Map<String, Object>> newSinks = new ArrayList<>();
                for (Map<String, Object> sink : sinks) {
                    Map<String, Object> newSink = new LinkedHashMap<>(sink);
                    for (String sensitiveOption : sensitiveOptions) {
                        newSink.computeIfPresent(sensitiveOption, processFunction);
                    }
                    newSinks.add(newSink);
                }
                configMap.put(Constants.SINK, newSinks);
            } else if (sinkVal instanceof Map) {
                Map<String, Object> sink = new LinkedHashMap<>((Map<String, Object>) sinkVal);
                for (Map.Entry<String, Object> entry : sink.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> pluginConfig =
                                new LinkedHashMap<>((Map<String, Object>) entry.getValue());
                        for (String sensitiveOption : sensitiveOptions) {
                            pluginConfig.computeIfPresent(sensitiveOption, processFunction);
                        }
                        sink.put(entry.getKey(), pluginConfig);
                    }
                }
                configMap.put(Constants.SINK, sink);
            }
        }

        return Config.of(configMap);
    }

    public static Set<String> getSensitiveOptions(Config config) {
        Set<String> sensitiveOptions = new HashSet<>();
        if (config != null
                && config.hasPath(Constants.ENV)
                && config.getConfig(Constants.ENV).hasPath(SHADE_OPTIONS_OPTION)) {
            sensitiveOptions.addAll(
                    config.getConfig(Constants.ENV).getStringList(SHADE_OPTIONS_OPTION));
        }
        sensitiveOptions.addAll(Arrays.asList(DEFAULT_SENSITIVE_KEYWORDS));
        return sensitiveOptions;
    }

    public static class Base64ConfigShade implements ConfigShade {

        private static final Base64.Encoder ENCODER = Base64.getEncoder();

        private static final Base64.Decoder DECODER = Base64.getDecoder();

        private static final String IDENTIFIER = "base64";

        @Override
        public String getIdentifier() {
            return IDENTIFIER;
        }

        @Override
        public String encrypt(String content) {
            return ENCODER.encodeToString(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String decrypt(String content) {
            return new String(DECODER.decode(content));
        }
    }

    public static Map<String, Object> configDesensitization(
            Map<String, Object> configMap, Set<String> sensitiveKeywords) {
        return configMap.entrySet().stream()
                .collect(
                        LinkedHashMap::new,
                        (m, p) -> {
                            String key = p.getKey();
                            Object value = p.getValue();
                            if (sensitiveKeywords.contains(key.toLowerCase())) {
                                if (value instanceof List<?>) {
                                    List<Object> maskedList =
                                            ((List<?>) value)
                                                    .stream()
                                                            .map(v -> "******")
                                                            .collect(Collectors.toList());
                                    m.put(key, maskedList);
                                } else {
                                    m.put(key, "******");
                                }
                            } else {
                                if (value instanceof Map<?, ?>) {
                                    m.put(
                                            key,
                                            configDesensitization(
                                                    (Map<String, Object>) value,
                                                    sensitiveKeywords));
                                } else if (value instanceof List<?>) {
                                    List<?> listValue = (List<?>) value;
                                    List<Object> newList =
                                            listValue.stream()
                                                    .map(
                                                            v -> {
                                                                if (v instanceof Map<?, ?>) {
                                                                    return configDesensitization(
                                                                            (Map<String, Object>) v,
                                                                            sensitiveKeywords);
                                                                } else {
                                                                    return v;
                                                                }
                                                            })
                                                    .collect(Collectors.toList());
                                    m.put(key, newList);
                                } else {
                                    m.put(key, value);
                                }
                            }
                        },
                        LinkedHashMap::putAll);
    }
}
