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

package org.apache.seatunnel.connectors.seatunnel.file.config;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;
import org.apache.seatunnel.api.options.ConnectorCommonOptions;
import org.apache.seatunnel.common.utils.DateTimeUtils;
import org.apache.seatunnel.common.utils.DateUtils;
import org.apache.seatunnel.common.utils.TimeUtils;

import java.util.List;

public class FileBaseOptions extends ConnectorCommonOptions {

    public static final ConfigEntry<String> FILENAME_EXTENSION =
            ConfigOption.key("filename_extension")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Filter filename extension, which used for filtering files with specific extension. Example: `csv` `.txt` `json` `.xml`.");

    public static final ConfigEntry<String> FILE_PATH =
            ConfigOption.key("path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The file path of target files");

    public static final ConfigEntry<String> ENCODING =
            ConfigOption.key("encoding")
                    .stringType()
                    .defaultValue("UTF-8")
                    .withDescription("The encoding of the file, e.g. UTF-8, ISO-8859-1....");

    public static final ConfigEntry<Boolean> PARSE_PARTITION_FROM_PATH =
            ConfigOption.key("parse_partition_from_path")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("Whether parse partition fields from file path");

    public static final ConfigEntry<String> HDFS_SITE_PATH =
            ConfigOption.key("hdfs_site_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The path of hdfs-site.xml");

    public static final ConfigEntry<String> REMOTE_USER =
            ConfigOption.key("remote_user")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The remote user name of hdfs");

    public static final ConfigEntry<String> KERBEROS_PRINCIPAL =
            ConfigOption.key("kerberos_principal")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Kerberos principal");

    public static final ConfigEntry<String> KRB5_PATH =
            ConfigOption.key("krb5_path")
                    .stringType()
                    .defaultValue("/etc/krb5.conf")
                    .withDescription(
                            "When use kerberos, we should set krb5 path file path such as '/seatunnel/krb5.conf' or use the default path '/etc/krb5.conf");

    public static final ConfigEntry<String> KERBEROS_KEYTAB_PATH =
            ConfigOption.key("kerberos_keytab_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Kerberos keytab file path");

    public static final ConfigEntry<Long> SKIP_HEADER_ROW_NUMBER =
            ConfigOption.key("skip_header_row_number")
                    .longType()
                    .defaultValue(0L)
                    .withDescription("The number of rows to skip");

    public static final ConfigEntry<Boolean> CSV_USE_HEADER_LINE =
            ConfigOption.key("csv_use_header_line")
                    .booleanType()
                    .defaultValue(Boolean.FALSE)
                    .withDescription(
                            "whether to use the header line to parse the file, only used when the file_format is csv");

    public static final ConfigEntry<List<String>> READ_PARTITIONS =
            ConfigOption.key("read_partitions")
                    .listType()
                    .noDefaultValue()
                    .withDescription("The partitions that the user want to read");

    public static final ConfigEntry<List<String>> READ_COLUMNS =
            ConfigOption.key("read_columns")
                    .listType()
                    .noDefaultValue()
                    .withDescription("The columns list that the user want to read");

    public static final ConfigEntry<String> SHEET_NAME =
            ConfigOption.key("sheet_name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("To be read sheet name,only valid for excel files");

    public static final ConfigEntry<ExcelEngine> EXCEL_ENGINE =
            ConfigOption.key("excel_engine")
                    .enumType(ExcelEngine.class)
                    .defaultValue(ExcelEngine.POI)
                    .withDescription("To switch excel read engine,  e.g. POI , EasyExcel");

    public static final ConfigEntry<String> XML_ROW_TAG =
            ConfigOption.key("xml_row_tag")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Specifies the tag name of the data rows within the XML file, only valid for XML files.");

    public static final ConfigEntry<Boolean> XML_USE_ATTR_FORMAT =
            ConfigOption.key("xml_use_attr_format")
                    .booleanType()
                    .noDefaultValue()
                    .withDescription(
                            "Specifies whether to process data using the tag attribute format, only valid for XML files.");

    public static final ConfigEntry<String> FILE_FILTER_PATTERN =
            ConfigOption.key("file_filter_pattern")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "File pattern. The connector will filter some files base on the pattern.");

    public static final ConfigEntry<CompressFormat> COMPRESS_CODEC =
            ConfigOption.key("compress_codec")
                    .enumType(CompressFormat.class)
                    .defaultValue(CompressFormat.NONE)
                    .withDescription("Compression codec");

    public static final ConfigEntry<ArchiveCompressFormat> ARCHIVE_COMPRESS_CODEC =
            ConfigOption.key("archive_compress_codec")
                    .enumType(ArchiveCompressFormat.class)
                    .defaultValue(ArchiveCompressFormat.NONE)
                    .withDescription("Archive compression codec");

    public static final ConfigEntry<DateUtils.Formatter> DATE_FORMAT_LEGACY =
            ConfigOption.key("date_format")
                    .enumType(DateUtils.Formatter.class)
                    .defaultValue(DateUtils.Formatter.YYYY_MM_DD)
                    .withDescription("Date format");

    public static final ConfigEntry<DateTimeUtils.Formatter> DATETIME_FORMAT_LEGACY =
            ConfigOption.key("datetime_format")
                    .enumType(DateTimeUtils.Formatter.class)
                    .defaultValue(DateTimeUtils.Formatter.YYYY_MM_DD_HH_MM_SS)
                    .withDescription("Datetime format");

    public static final ConfigEntry<TimeUtils.Formatter> TIME_FORMAT_LEGACY =
            ConfigOption.key("time_format")
                    .enumType(TimeUtils.Formatter.class)
                    .defaultValue(TimeUtils.Formatter.HH_MM_SS)
                    .withDescription("Time format");
}
