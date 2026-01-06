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

import org.apache.seatunnel.common.config.ConfigType;
import org.apache.seatunnel.common.parser.ConfigParser;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;

public class ConfigLoader {

    private ConfigLoader() {}

    public static Config load(Path path) {
        String fileName = path.getFileName().toString();
        ConfigType type = inferType(fileName);
        return load(path, type);
    }

    public static Config load(Path path, ConfigType type) {
        ConfigParser parser = findParser(type);
        return ReadonlyConfig.fromMap(parser.parse(path));
    }

    public static Config load(String content, ConfigType type) {
        ConfigParser parser = findParser(type);
        return ReadonlyConfig.fromMap(parser.parse(content));
    }

    private static ConfigType inferType(String fileName) {
        for (ConfigType type : ConfigType.values()) {
            for (String extension : type.getExtensions()) {
                if (fileName.endsWith(extension)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported file type: " + fileName);
    }

    private static ConfigParser findParser(ConfigType type) {
        ServiceLoader<ConfigParser> loader = ServiceLoader.load(ConfigParser.class);
        Iterator<ConfigParser> iterator = loader.iterator();
        while (iterator.hasNext()) {
            ConfigParser parser = iterator.next();
            if (parser.getConfigType() == type) {
                return parser;
            }
        }
        throw new UnsupportedOperationException("No parser found for type: " + type);
    }
}
