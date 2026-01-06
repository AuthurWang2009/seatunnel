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

import org.apache.seatunnel.api.config.util.ConfigUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ReadonlyConfig extends AbstractConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

    /** Stores the concrete key/value pairs of this configuration object. */
    protected final Map<String, Object> confData;

    public ReadonlyConfig(Map<String, Object> confData) {
        this.confData = confData;
    }

    public static ReadonlyConfig fromMap(Map<String, Object> map) {
        return new ReadonlyConfig(map);
    }

    /** @deprecated Please use {@link ReadonlyConfig#fromMap(Map)} instead. */
    @Deprecated
    public static ReadonlyConfig fromConfig(com.typesafe.config.Config config) {
        try {
            return fromMap(
                    JACKSON_MAPPER.readValue(
                            config.root().render(ConfigRenderOptions.concise()),
                            new TypeReference<LinkedHashMap<String, Object>>() {}));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Json parsing exception.", e);
        }
    }

    @Override
    public <T> T get(ConfigEntry<T> option) {
        return getOptional(option).orElseGet(option::defaultValue);
    }

    /**
     * Transform to Config todo: This method should be removed after we remove Config
     *
     * @return Config
     * @deprecated Please use ReadonlyConfig directly
     */
    @Deprecated
    public com.typesafe.config.Config toConfig() {
        return ConfigFactory.parseMap(confData);
    }

    @Override
    public Map<String, String> toMap() {
        if (confData.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>();
        toMap(result);
        return result;
    }

    public void toMap(Map<String, String> result) {
        if (confData.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : confData.entrySet()) {
            result.put(entry.getKey(), ConfigUtil.convertToJsonString(entry.getValue()));
        }
    }

    public Map<String, Object> getSourceMap() {
        return confData;
    }

    @Override
    public <T> Optional<T> getOptional(ConfigEntry<T> option) {
        if (option == null) {
            throw new NullPointerException("Option not be null.");
        }
        Object value = getValue(option.key());
        if (value == null) {
            for (String fallbackKey : option.getFallbackKeys()) {
                value = getValue(fallbackKey);
                if (value != null) {
                    log.warn(
                            "Please use the new key '{}' instead of the deprecated key '{}'.",
                            option.key(),
                            fallbackKey);
                    break;
                }
            }
        }
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(ConfigUtil.convertValue(value, option));
    }

    @Override
    protected Object internalGet(String key) {
        return getValue(key);
    }

    @Override
    public Config getConfig(String key) {
        Object value = internalGet(key);
        if (value instanceof Map) {
            return new ReadonlyConfig((Map<String, Object>) value);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Config withValue(String key, Object value) {
        // Deep copy of the current map to ensure immutability
        Map<String, Object> newConfData = deepCopy(this.confData);

        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = newConfData;

        for (int i = 0; i < keys.length - 1; i++) {
            String currentKey = keys[i];
            Object val = currentMap.get(currentKey);
            if (!(val instanceof Map)) {
                val = new LinkedHashMap<>();
                currentMap.put(currentKey, val);
            }
            currentMap = (Map<String, Object>) val;
        }

        currentMap.put(keys[keys.length - 1], value);
        return new ReadonlyConfig(newConfData);
    }

    // Typo fix wihValue -> withValue helper if needed or direct

    @Override
    public Config merge(Config other) {
        if (!(other instanceof ReadonlyConfig)) {
            throw new IllegalArgumentException("Can only merge with ReadonlyConfig");
        }
        Map<String, Object> otherMap = ((ReadonlyConfig) other).confData;
        Map<String, Object> newMap = deepCopy(this.confData);
        mergeMaps(newMap, otherMap);
        return new ReadonlyConfig(newMap);
    }

    public Object getValue(String key) {
        if (this.confData.containsKey(key)) {
            return this.confData.get(key);
        } else {
            String[] keys = key.split("\\.");
            Map<String, Object> data = this.confData;
            Object value = null;
            for (int i = 0; i < keys.length; i++) {
                value = data.get(keys[i]);
                if (i < keys.length - 1) {
                    if (!(value instanceof Map)) {
                        return null;
                    } else {
                        data = (Map<String, Object>) value;
                    }
                }
            }
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeMaps(Map<String, Object> base, Map<String, Object> overlay) {
        for (Map.Entry<String, Object> entry : overlay.entrySet()) {
            if (entry.getValue() instanceof Map && base.get(entry.getKey()) instanceof Map) {
                mergeMaps(
                        (Map<String, Object>) base.get(entry.getKey()),
                        (Map<String, Object>) entry.getValue());
            } else {
                base.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> original) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                copy.put(entry.getKey(), deepCopy((Map<String, Object>) value));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (String s : this.confData.keySet()) {
            hash ^= s.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReadonlyConfig)) {
            return false;
        }
        Map<String, Object> otherConf = ((ReadonlyConfig) obj).confData;
        return this.confData.equals(otherConf);
    }

    @Override
    public String toString() {
        return ConfigUtil.convertToJsonString(this.confData);
    }
}
