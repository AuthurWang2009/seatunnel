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

package org.apache.seatunnel.connectors.seatunnel.jdbc.config;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;
import org.apache.seatunnel.api.sink.DataSaveMode;
import org.apache.seatunnel.api.sink.SchemaSaveMode;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.dialectenum.FieldIdeEnum;

import java.util.List;

public class JdbcSinkOptions extends JdbcCommonOptions {

    public static final ConfigEntry<String> DATABASE =
            ConfigOption.key("database").stringType().noDefaultValue().withDescription("database");

    public static final ConfigEntry<String> TABLE =
            ConfigOption.key("table").stringType().noDefaultValue().withDescription("table");

    public static final ConfigEntry<SchemaSaveMode> SCHEMA_SAVE_MODE =
            ConfigOption.key("schema_save_mode")
                    .enumType(SchemaSaveMode.class)
                    .defaultValue(SchemaSaveMode.CREATE_SCHEMA_WHEN_NOT_EXIST)
                    .withDescription("schema_save_mode");

    public static final ConfigEntry<DataSaveMode> DATA_SAVE_MODE =
            ConfigOption.key("data_save_mode")
                    .enumType(DataSaveMode.class)
                    .defaultValue(DataSaveMode.APPEND_DATA)
                    .withDescription("data_save_mode");

    public static final ConfigEntry<String> CUSTOM_SQL =
            ConfigOption.key("custom_sql")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("custom_sql");

    public static final ConfigEntry<Boolean> GENERATE_SINK_SQL =
            ConfigOption.key("generate_sink_sql")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("generate sql using the database table");

    public static final ConfigEntry<Boolean> IS_EXACTLY_ONCE =
            ConfigOption.key("is_exactly_once")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("exactly once");

    public static final ConfigEntry<Boolean> AUTO_COMMIT =
            ConfigOption.key("auto_commit")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("auto commit");

    public static final ConfigEntry<Integer> MAX_RETRIES =
            ConfigOption.key("max_retries")
                    .intType()
                    .defaultValue(0)
                    .withDescription("max_retired");

    public static final ConfigEntry<String> XA_DATA_SOURCE_CLASS_NAME =
            ConfigOption.key("xa_data_source_class_name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("data source class name");

    public static final ConfigEntry<Integer> MAX_COMMIT_ATTEMPTS =
            ConfigOption.key("max_commit_attempts")
                    .intType()
                    .defaultValue(3)
                    .withDescription("max commit attempts");

    public static final ConfigEntry<Integer> BATCH_SIZE =
            ConfigOption.key("batch_size")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("batch size");

    public static final ConfigEntry<Integer> TRANSACTION_TIMEOUT_SEC =
            ConfigOption.key("transaction_timeout_sec")
                    .intType()
                    .defaultValue(-1)
                    .withDescription("transaction timeout (second)");

    public static final ConfigEntry<Boolean> ENABLE_UPSERT =
            ConfigOption.key("enable_upsert")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("enable upsert by primary_keys exist");

    public static final ConfigEntry<List<String>> PRIMARY_KEYS =
            ConfigOption.key("primary_keys")
                    .listType()
                    .noDefaultValue()
                    .withDescription("primary keys");

    public static final ConfigEntry<Boolean> IS_PRIMARY_KEY_UPDATED =
            ConfigOption.key("is_primary_key_updated")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription(
                            "is the primary key updated when performing an update operation");

    public static final ConfigEntry<Boolean> SUPPORT_UPSERT_BY_INSERT_ONLY =
            ConfigOption.key("support_upsert_by_insert_only")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("support upsert by insert only");

    public static final ConfigEntry<Boolean> USE_COPY_STATEMENT =
            ConfigOption.key("use_copy_statement")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("support copy in statement (postgresql)");

    public static final ConfigEntry<FieldIdeEnum> FIELD_IDE =
            ConfigOption.key("field_ide")
                    .enumType(FieldIdeEnum.class)
                    .noDefaultValue()
                    .withDescription("Whether case conversion is required");

    public static final ConfigEntry<String> TABLE_PREFIX =
            ConfigOption.key("tablePrefix")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The table prefix name added when the table is automatically created");

    public static final ConfigEntry<String> TABLE_SUFFIX =
            ConfigOption.key("tableSuffix")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The table suffix name added when the table is automatically created");

    public static final ConfigEntry<Boolean> CREATE_INDEX =
            ConfigOption.key("create_index")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("Create index or not when auto create table");
}
