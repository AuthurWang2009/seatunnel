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

package org.apache.seatunnel.api.table.catalog;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;

public class InMemoryCatalogOptionRule {

    public static final ConfigEntry<String> username =
            ConfigOption.key("username").stringType().noDefaultValue().withDescription("username");

    public static final ConfigEntry<String> password =
            ConfigOption.key("password").stringType().noDefaultValue().withDescription("password");

    public static final ConfigEntry<String> host =
            ConfigOption.key("host").stringType().defaultValue("localhost").withDescription("host");

    public static final ConfigEntry<Integer> port =
            ConfigOption.key("port").intType().defaultValue(5081).withDescription("port");
}
