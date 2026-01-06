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

package org.apache.seatunnel.api.options.table;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public interface PrimaryKeyOptions {

    ConfigEntry<Map<String, Object>> PRIMARY_KEY =
            ConfigOption.key("primaryKey")
                    .type(new TypeReference<Map<String, Object>>() {})
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Fields");

    ConfigEntry<String> PRIMARY_KEY_NAME =
            ConfigOption.key("name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Primary Key Name");

    ConfigEntry<List<String>> PRIMARY_KEY_COLUMNS =
            ConfigOption.key("columnNames")
                    .listType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Primary Key Columns");
}
