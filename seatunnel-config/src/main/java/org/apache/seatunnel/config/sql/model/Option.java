/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.seatunnel.config.sql.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class Option {
    private String key;
    private Object value;

    public static Option of(String key, String value) {
        // Use ConfigFactory to infer the type (Integer, Boolean, String, List, Map)
        // by wrapping it in a simple HOCON key-value pair.
        try {
            // We use "v=" + value to let HOCON parse the value.
            // e.g. "10" -> Integer(10), "true" -> Boolean(true), "{...}" -> ConfigObject
            Object typedValue =
                    com.typesafe.config.ConfigFactory.parseString("v=" + value)
                            .getValue("v")
                            .unwrapped();
            return new Option(key, typedValue);
        } catch (Exception e) {
            // Fallback to string if parsing fails
            return new Option(key, value);
        }
    }

    public static Option of(String key, Object value) {
        return new Option(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        return Objects.equals(key, option.key);
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
