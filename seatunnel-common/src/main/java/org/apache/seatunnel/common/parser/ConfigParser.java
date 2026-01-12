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

package org.apache.seatunnel.common.parser;

import org.apache.seatunnel.common.config.ConfigType;

import java.nio.file.Path;
import java.util.Map;

public interface ConfigParser {
    ConfigType getConfigType();

    Map<String, Object> parse(Path path);

    Map<String, Object> parse(String content);

    /**
     * Parse configuration from file with variable substitution.
     *
     * @param path the path to the configuration file
     * @param variables user-provided variables for substitution (can be null)
     * @return the parsed configuration with variables resolved
     */
    default Map<String, Object> parse(Path path, Map<String, Object> variables) {
        return ConfigVariableResolver.resolve(parse(path), variables);
    }

    /**
     * Parse configuration from string content with variable substitution.
     *
     * @param content the configuration content
     * @param variables user-provided variables for substitution (can be null)
     * @return the parsed configuration with variables resolved
     */
    default Map<String, Object> parse(String content, Map<String, Object> variables) {
        return ConfigVariableResolver.resolve(parse(content), variables);
    }
}
