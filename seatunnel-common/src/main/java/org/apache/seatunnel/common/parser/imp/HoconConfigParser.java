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

package org.apache.seatunnel.common.parser.imp;

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
        Config config =
                ConfigFactory.parseFile(
                        path.toFile(), ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF));
        // We must use ConfigFactory.systemProperties() here because ConfigLoader returns a Map,
        // which implicitly requires eager resolution. If we don't resolve system properties here,
        // they will be lost when the Config is unwrapped to a Map.
        return config.resolveWith(
                        ConfigFactory.systemProperties(),
                        ConfigResolveOptions.defaults().setAllowUnresolved(true))
                .root()
                .unwrapped();
    }

    @Override
    public Map<String, Object> parse(String content) {
        Config config =
                ConfigFactory.parseString(
                        content, ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF));
        return config.resolveWith(
                        ConfigFactory.systemProperties(),
                        ConfigResolveOptions.defaults().setAllowUnresolved(true))
                .root()
                .unwrapped();
    }
}
