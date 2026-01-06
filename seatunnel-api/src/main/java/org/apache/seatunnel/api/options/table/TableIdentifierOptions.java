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

public interface TableIdentifierOptions {

    ConfigEntry<Boolean> SCHEMA_FIRST =
            ConfigOption.key("schema_first")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Parse Schema First from table");

    ConfigEntry<String> TABLE =
            ConfigOption.key("table")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Full Table Name");

    ConfigEntry<String> TABLE_COMMENT =
            ConfigOption.key("comment")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Table Comment");

    ConfigEntry<String> DATABASE_NAME =
            ConfigOption.key("database_name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Database Name");

    ConfigEntry<String> SCHEMA_NAME =
            ConfigOption.key("schema_name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Table Name");

    ConfigEntry<String> TABLE_NAME =
            ConfigOption.key("table_name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SeaTunnel Schema Table Name");
}
