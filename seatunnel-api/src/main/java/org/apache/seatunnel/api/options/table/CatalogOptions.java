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

public interface CatalogOptions {

    @Deprecated
    ConfigEntry<Map<String, String>> CATALOG_OPTIONS =
            ConfigOption.key("catalog")
                    .mapType()
                    .noDefaultValue()
                    .withDescription("configuration options for the catalog.");

    ConfigEntry<String> CATALOG_NAME =
            ConfigOption.key("name").stringType().noDefaultValue().withDescription("catalog name");

    ConfigEntry<List<String>> TABLE_NAMES =
            ConfigOption.key("table-names")
                    .listType()
                    .noDefaultValue()
                    .withDescription(
                            "List of table names of databases to capture."
                                    + "The table name needs to include the database name, for example: database_name.table_name");

    ConfigEntry<String> DATABASE_PATTERN =
            ConfigOption.key("database-pattern")
                    .stringType()
                    .defaultValue(".*")
                    .withDescription("The database names RegEx of the database to capture.");

    ConfigEntry<String> TABLE_PATTERN =
            ConfigOption.key("table-pattern")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The table names RegEx of the database to capture."
                                    + "The table name needs to include the database name, for example: database_.*\\.table_.*");

    ConfigEntry<List<Map<String, Object>>> TABLE_LIST =
            ConfigOption.key("table_list")
                    .type(new TypeReference<List<Map<String, Object>>>() {})
                    .noDefaultValue()
                    .withDescription(
                            "SeaTunnel Multi Table Schema, acts on structed data sources. "
                                    + "such as jdbc, paimon, doris, etc");
}
