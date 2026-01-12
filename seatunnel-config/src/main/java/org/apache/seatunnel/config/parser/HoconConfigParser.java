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

package org.apache.seatunnel.config.parser;

import org.apache.seatunnel.common.config.ConfigType;
import org.apache.seatunnel.common.parser.ConfigParser;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigSyntax;

import java.nio.file.Path;
import java.util.Map;

public class HoconConfigParser implements ConfigParser {

    @Override
    public ConfigType getConfigType() {
        return ConfigType.HOCON;
    }

    @Override
    public Map<String, Object> parse(Path path) {
        return parse(path, null);
    }

    @Override
    public Map<String, Object> parse(String content) {
        return parse(content, null);
    }

    @Override
    public Map<String, Object> parse(Path path, Map<String, Object> variables) {
        Config config =
                ConfigFactory.parseFile(
                        path.toFile(), ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF));
        ConfigFactory.invalidateCaches();
        return resolveConfig(config, variables);
    }

    @Override
    public Map<String, Object> parse(String content, Map<String, Object> variables) {
        Config config =
                ConfigFactory.parseString(
                        content, ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF));
        return resolveConfig(config, variables);
    }

    /**
     * Resolve config with variable substitution. Priority: userVariables > systemProperties >
     * inlineDefault
     */
    private Map<String, Object> resolveConfig(Config config, Map<String, Object> userVariables) {
        // Build fallback chain: userVariables (highest priority) -> systemProperties
        Config fallback = ConfigFactory.systemProperties();
        if (userVariables != null && !userVariables.isEmpty()) {
            fallback = ConfigFactory.parseMap(toStringMap(userVariables)).withFallback(fallback);
        }

        return config.resolveWith(
                        fallback, ConfigResolveOptions.defaults().setAllowUnresolved(true))
                .root()
                .unwrapped();
    }

    /** Convert Map<String, Object> to Map<String, String> for HOCON ConfigFactory.parseMap */
    private Map<String, String> toStringMap(Map<String, Object> map) {
        Map<String, String> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return result;
    }
}
