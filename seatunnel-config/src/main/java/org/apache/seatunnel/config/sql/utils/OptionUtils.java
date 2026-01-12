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

package org.apache.seatunnel.config.sql.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class OptionUtils {

    public static Map<String, String> parseOptions(String optionsStr) {
        Map<String, String> options = new LinkedHashMap<>();
        if (optionsStr == null || optionsStr.isEmpty()) {
            return options;
        }

        // Remove outer parens if present
        optionsStr = optionsStr.trim();
        if (optionsStr.startsWith("(") && optionsStr.endsWith(")")) {
            optionsStr = optionsStr.substring(1, optionsStr.length() - 1);
        }

        // Simple state machine to split by comma, respecting quotes
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        boolean inKey = true;
        boolean inQuote = false;
        char quoteChar = 0;

        // We accumulate partial tokens
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < optionsStr.length(); i++) {
            char c = optionsStr.charAt(i);

            if (inQuote) {
                if (c == quoteChar) {
                    // check for escapement ?? SQL uses '' for '
                    if (i + 1 < optionsStr.length() && optionsStr.charAt(i + 1) == quoteChar) {
                        currentToken.append(c);
                        i++; // skip next valid quote
                    } else {
                        inQuote = false;
                        currentToken.append(c);
                    }
                } else {
                    currentToken.append(c);
                }
            } else {
                if (c == '\'' || c == '"') {
                    inQuote = true;
                    quoteChar = c;
                    currentToken.append(c);
                } else if (c == '=' && inKey) {
                    // switch to value
                    keyBuilder.append(currentToken.toString().trim());
                    currentToken.setLength(0);
                    inKey = false;
                } else if (c == ',') {
                    // End of pair
                    valueBuilder.append(currentToken.toString().trim());
                    currentToken.setLength(0);

                    addOption(options, keyBuilder.toString(), valueBuilder.toString());
                    keyBuilder.setLength(0);
                    valueBuilder.setLength(0);
                    inKey = true;
                } else {
                    currentToken.append(c);
                }
            }
        }

        // Add last pair
        if (keyBuilder.length() > 0 || currentToken.length() > 0) {
            valueBuilder.append(currentToken.toString().trim());
            addOption(options, keyBuilder.toString(), valueBuilder.toString());
        }

        return options;
    }

    private static void addOption(Map<String, String> map, String key, String value) {
        key = cleanQuote(key);
        value = cleanQuote(value);
        if (!key.isEmpty()) {
            map.put(key, value);
        }
    }

    private static String cleanQuote(String str) {
        if (str.startsWith("'") && str.endsWith("'") && str.length() >= 2) {
            return str.substring(1, str.length() - 1);
        }
        if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
}
