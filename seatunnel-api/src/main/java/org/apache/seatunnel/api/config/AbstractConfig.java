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

import java.util.Optional;

public abstract class AbstractConfig implements Config {

    @Override
    public <T> T get(ConfigEntry<T> option) {
        return getOptional(option).orElseGet(option::defaultValue);
    }

    public <T> Optional<T> getOptional(ConfigEntry<T> option) {
        if (option == null) {
            throw new NullPointerException("Option not be null.");
        }
        Object value = internalGet(option.key());
        if (value == null) {
            for (String fallbackKey : option.getFallbackKeys()) {
                value = internalGet(fallbackKey);
                if (value != null) {
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
    public String getString(String key) {
        Object value = internalGet(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '" + key + "' not found");
        }
        return ConfigUtil.convertValue(value, String.class);
    }

    @Override
    public int getInt(String key) {
        Object value = internalGet(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '" + key + "' not found");
        }
        return ConfigUtil.convertToInt(value);
    }

    @Override
    public boolean getBoolean(String key) {
        Object value = internalGet(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '" + key + "' not found");
        }
        return ConfigUtil.convertToBoolean(value);
    }

    protected abstract Object internalGet(String key);

    @Override
    public boolean hasPath(String key) {
        return internalGet(key) != null;
    }

    @Override
    public java.util.List<String> getStringList(String key) {
        Object value = internalGet(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '" + key + "' not found");
        }
        return ConfigUtil.convertToList(value, String.class);
    }
}
