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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class JsonConfigParser implements ConfigParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ConfigType getConfigType() {
        return ConfigType.JSON;
    }

    @Override
    public Map<String, Object> parse(Path path) {
        try {
            return MAPPER.readValue(path.toFile(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse json file: " + path, e);
        }
    }

    @Override
    public Map<String, Object> parse(String content) {
        try {
            return MAPPER.readValue(content, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse json content", e);
        }
    }
}
