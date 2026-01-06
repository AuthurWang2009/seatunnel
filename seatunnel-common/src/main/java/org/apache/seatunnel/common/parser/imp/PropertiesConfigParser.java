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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesConfigParser implements ConfigParser {

    @Override
    public ConfigType getConfigType() {
        return ConfigType.PROPERTIES;
    }

    @Override
    public Map<String, Object> parse(Path path) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            properties.load(fis);
            return convertPropertiesToMap(properties);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse properties file: " + path, e);
        }
    }

    @Override
    public Map<String, Object> parse(String content) {
        Properties properties = new Properties();
        try (StringReader reader = new StringReader(content)) {
            properties.load(reader);
            return convertPropertiesToMap(properties);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse properties content", e);
        }
    }

    private Map<String, Object> convertPropertiesToMap(Properties properties) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
        return map;
    }
}
