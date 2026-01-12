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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for resolving variable placeholders in configuration values.
 *
 * <p>Supports two variable formats:
 *
 * <ul>
 *   <li>{@code ${varName}} - variable without default value
 *   <li>{@code ${varName:defaultValue}} - variable with inline default value
 * </ul>
 *
 * <p>Resolution priority (highest to lowest):
 *
 * <ol>
 *   <li>User-provided variables ({@code Map<String, Object>})
 *   <li>System properties ({@code System.getProperty()})
 *   <li>Inline default value ({@code :defaultValue} syntax)
 *   <li>Leave unresolved (if all above fail)
 * </ol>
 */
public class ConfigVariableResolver {

    private static final String PLACEHOLDER_REGEX = "\\$\\{([^:{}]+)(?::([^}]*))?\\}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);

    private ConfigVariableResolver() {}

    /**
     * Resolve variable placeholders in the given configuration map.
     *
     * @param config the configuration map to resolve
     * @param userVariables user-provided variables (can be null)
     * @return a new map with resolved variables
     */
    public static Map<String, Object> resolve(
            Map<String, Object> config, Map<String, Object> userVariables) {
        if (config == null || config.isEmpty()) {
            return config;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            result.put(entry.getKey(), resolveValue(entry.getValue(), userVariables));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object resolveValue(Object value, Map<String, Object> userVariables) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return resolveString((String) value, userVariables);
        }
        if (value instanceof Map) {
            return resolve((Map<String, Object>) value, userVariables);
        }
        if (value instanceof List) {
            return ((List<?>) value)
                    .stream()
                            .map(item -> resolveValue(item, userVariables))
                            .collect(Collectors.toList());
        }
        return value;
    }

    private static String resolveString(String input, Map<String, Object> userVariables) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultValue = matcher.group(2);

            String replacement = resolveVariable(varName, defaultValue, userVariables);
            if (replacement != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            // If replacement is null, leave the original placeholder unchanged
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /** Resolve a single variable by priority: userVariables > systemProperties > defaultValue */
    private static String resolveVariable(
            String varName, String defaultValue, Map<String, Object> userVariables) {
        // 1. Check user-provided variables first
        if (userVariables != null && userVariables.containsKey(varName)) {
            Object value = userVariables.get(varName);
            return value != null ? String.valueOf(value) : null;
        }

        // 2. Check system properties
        String systemValue = System.getProperty(varName);
        if (systemValue != null) {
            return systemValue;
        }

        // 3. Use inline default value if provided
        if (defaultValue != null) {
            return defaultValue;
        }

        // 4. Return null to leave placeholder unresolved
        return null;
    }
}
