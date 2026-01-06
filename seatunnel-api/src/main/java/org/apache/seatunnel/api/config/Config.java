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

import java.io.Serializable;
import java.util.Map;

public interface Config extends Serializable {

    String getString(String key);

    int getInt(String key);

    boolean getBoolean(String key);

    Config getConfig(String key);

    <T> T get(ConfigEntry<T> option);

    Object getValue(String key);

    <T> java.util.Optional<T> getOptional(ConfigEntry<T> option);

    Map<String, String> toMap();

    Config withValue(String key, Object value);

    Config merge(Config other);

    boolean hasPath(String key);

    java.util.List<String> getStringList(String key);
}
