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

public interface ColumnOptions {

    // todo: how to define List<Map<String, Object>>
    ConfigEntry<List<Map<String, Object>>> COLUMNS =
            ConfigOption.key("columns")
                    .type(new TypeReference<List<Map<String, Object>>>() {})
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Columns");

    ConfigEntry<String> COLUMN_NAME =
            ConfigOption.key("name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Column Name");

    ConfigEntry<String> TYPE =
            ConfigOption.key("type")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Column Type");

    ConfigEntry<Integer> COLUMN_SCALE =
            ConfigOption.key("columnScale")
                    .intType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Column scale");

    ConfigEntry<Long> COLUMN_LENGTH =
            ConfigOption.key("columnLength")
                    .longType()
                    .defaultValue(0L)
                    .withDescription("SeaTunnel Schema Column Length");

    ConfigEntry<Boolean> NULLABLE =
            ConfigOption.key("nullable")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("SeaTunnel Schema Column Nullable");

    ConfigEntry<Object> DEFAULT_VALUE =
            ConfigOption.key("defaultValue")
                    .objectType(Object.class)
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Column Default Value");

    ConfigEntry<String> COLUMN_COMMENT =
            ConfigOption.key("comment")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Column Comment");
}
