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

package org.apache.seatunnel.connectors.seatunnel.hive.config;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;

import java.util.HashMap;
import java.util.Map;

public class HiveConfig {
    public static final ConfigEntry<String> TABLE_NAME =
            ConfigOption.key("table_name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Hive table name");
    public static final ConfigEntry<String> METASTORE_URI =
            ConfigOption.key("metastore_uri")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Hive metastore uri");

    public static final ConfigEntry<Boolean> ABORT_DROP_PARTITION_METADATA =
            ConfigOption.key("abort_drop_partition_metadata")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            "Flag to decide whether to drop partition metadata from Hive Metastore during an abort operation. Note: this only affects the metadata in the metastore, the data in the partition will always be deleted(data generated during the synchronization process).");

    public static final ConfigEntry<String> HIVE_SITE_PATH =
            ConfigOption.key("hive_site_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The path of hive-site.xml");

    public static final ConfigEntry<Map<String, String>> HADOOP_CONF =
            ConfigOption.key("hive.hadoop.conf")
                    .mapType()
                    .defaultValue(new HashMap<>())
                    .withDescription("Properties in hadoop conf");

    public static final ConfigEntry<String> HADOOP_CONF_PATH =
            ConfigOption.key("hive.hadoop.conf-path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The specified loading path for the 'core-site.xml', 'hdfs-site.xml' files");
}
