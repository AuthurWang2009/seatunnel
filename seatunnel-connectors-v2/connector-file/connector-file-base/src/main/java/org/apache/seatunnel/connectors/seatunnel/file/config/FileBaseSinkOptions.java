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
import org.apache.seatunnel.api.sink.DataSaveMode;
import org.apache.seatunnel.api.sink.SchemaSaveMode;
import org.apache.seatunnel.common.utils.DateUtils;
import org.apache.seatunnel.format.csv.constant.CsvStringQuoteMode;
import org.apache.seatunnel.format.text.constant.TextFormatConstant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.seatunnel.api.sink.DataSaveMode.APPEND_DATA;
import static org.apache.seatunnel.api.sink.DataSaveMode.DROP_DATA;
import static org.apache.seatunnel.api.sink.DataSaveMode.ERROR_WHEN_DATA_EXISTS;

public class FileBaseSinkOptions extends FileBaseOptions {
    public static final String SEATUNNEL = "seatunnel";
    public static final String NON_PARTITION = "NON_PARTITION";
    public static final String TRANSACTION_ID_SPLIT = "_";
    public static final String TRANSACTION_EXPRESSION = "transactionId";
    public static final String DEFAULT_FIELD_DELIMITER = TextFormatConstant.SEPARATOR[0];
    public static final String DEFAULT_ROW_DELIMITER = "\n";
    public static final String DEFAULT_PARTITION_DIR_EXPRESSION =
            "${k0}=${v0}/${k1}=${v1}/.../${kn}=${vn}/";
    public static final String DEFAULT_TMP_PATH = "/tmp/seatunnel";
    public static final String DEFAULT_FILE_NAME_EXPRESSION = "${transactionId}";
    public static final int DEFAULT_BATCH_SIZE = 1000000;

    public static final ConfigEntry<CompressFormat> COMPRESS_CODEC =
            ConfigOption.key("compress_codec")
                    .enumType(CompressFormat.class)
                    .defaultValue(CompressFormat.NONE)
                    .withDescription("Compression codec");

    // TODO：Compression is supported during write
    public static final ConfigEntry<ArchiveCompressFormat> ARCHIVE_COMPRESS_CODEC =
            ConfigOption.key("archive_compress_codec")
                    .enumType(ArchiveCompressFormat.class)
                    .defaultValue(ArchiveCompressFormat.NONE)
                    .withDescription("Archive compression codec");

    public static final ConfigEntry<CompressFormat> TXT_COMPRESS =
            ConfigOption.key("compress_codec")
                    .singleChoice(
                            CompressFormat.class,
                            Arrays.asList(CompressFormat.NONE, CompressFormat.LZO))
                    .defaultValue(CompressFormat.NONE)
                    .withDescription("Txt file supported compression");

    public static final ConfigEntry<CompressFormat> PARQUET_COMPRESS =
            ConfigOption.key("compress_codec")
                    .singleChoice(
                            CompressFormat.class,
                            Arrays.asList(
                                    CompressFormat.NONE,
                                    CompressFormat.LZO,
                                    CompressFormat.SNAPPY,
                                    CompressFormat.LZ4,
                                    CompressFormat.GZIP,
                                    CompressFormat.BROTLI,
                                    CompressFormat.ZSTD))
                    .defaultValue(CompressFormat.NONE)
                    .withDescription("Parquet file supported compression");

    public static final ConfigEntry<CompressFormat> ORC_COMPRESS =
            ConfigOption.key("compress_codec")
                    .singleChoice(
                            CompressFormat.class,
                            Arrays.asList(
                                    CompressFormat.NONE,
                                    CompressFormat.LZO,
                                    CompressFormat.SNAPPY,
                                    CompressFormat.LZ4,
                                    CompressFormat.ZLIB))
                    .defaultValue(CompressFormat.NONE)
                    .withDescription("Orc file supported compression");

    public static final ConfigEntry<String> FILE_PATH =
            ConfigOption.key("path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The file path of target files");

    public static final ConfigEntry<String> FIELD_DELIMITER =
            ConfigOption.key("field_delimiter")
                    .stringType()
                    .defaultValue(DEFAULT_FIELD_DELIMITER)
                    .withDescription(
                            "The separator between columns in a row of data. Only needed by `text` and `csv` file format");

    public static final ConfigEntry<Integer> SHEET_MAX_ROWS =
            ConfigOption.key("sheet_max_rows")
                    .intType()
                    .defaultValue(1048576)
                    .withDescription("Only needed by `excel` file format");

    public static final ConfigEntry<String> ROW_DELIMITER =
            ConfigOption.key("row_delimiter")
                    .stringType()
                    .defaultValue(DEFAULT_ROW_DELIMITER)
                    .withDescription(
                            "The separator between rows in a file. Only needed by `text`, `csv` and `json` file format");

    public static final ConfigEntry<Boolean> HAVE_PARTITION =
            ConfigOption.key("have_partition")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Whether need partition when write data");

    public static final ConfigEntry<List<String>> PARTITION_BY =
            ConfigOption.key("partition_by")
                    .listType()
                    .noDefaultValue()
                    .withDescription("Partition keys list, Only used when have_partition is true");

    public static final ConfigEntry<String> PARTITION_DIR_EXPRESSION =
            ConfigOption.key("partition_dir_expression")
                    .stringType()
                    .defaultValue(DEFAULT_PARTITION_DIR_EXPRESSION)
                    .withDescription(
                            "Only used when have_partition is true. If the `partition_by` is specified, "
                                    + "we will generate the corresponding partition directory based on the partition information, "
                                    + "and the final file will be placed in the partition directory. "
                                    + "Default `partition_dir_expression` is `${k0}=${v0}/${k1}=${v1}/.../${kn}=${vn}/`. "
                                    + "`k0` is the first partition field and `v0` is the value of the first partition field.");

    public static final ConfigEntry<Boolean> IS_PARTITION_FIELD_WRITE_IN_FILE =
            ConfigOption.key("is_partition_field_write_in_file")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            "Only used when have_partition is true. Whether to write partition fields to file");

    public static final ConfigEntry<String> TMP_PATH =
            ConfigOption.key("tmp_path")
                    .stringType()
                    .defaultValue(DEFAULT_TMP_PATH)
                    .withDescription("Data write temporary path");

    public static final ConfigEntry<Boolean> CUSTOM_FILENAME =
            ConfigOption.key("custom_filename")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Whether custom the output filename");

    public static final ConfigEntry<String> FILE_NAME_EXPRESSION =
            ConfigOption.key("file_name_expression")
                    .stringType()
                    .defaultValue(DEFAULT_FILE_NAME_EXPRESSION)
                    .withDescription(
                            "Only used when `custom_filename` is true. `file_name_expression` describes the file expression which will be created into the `path`. "
                                    + "We can add the variable `${now}` or `${uuid}` in the `file_name_expression`, "
                                    + "like `test_${uuid}_${now}`,`${now}` represents the current time, "
                                    + "and its format can be defined by specifying the option `filename_time_format`.");

    public static final ConfigEntry<Boolean> SINGLE_FILE_MODE =
            ConfigOption.key("single_file_mode")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            "Whether to write all data to a single file in each parallelism task");

    public static final ConfigEntry<Boolean> CREATE_EMPTY_FILE_WHEN_NO_DATA =
            ConfigOption.key("create_empty_file_when_no_data")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            "Whether to generate an empty file when there is no data to write");

    public static final ConfigEntry<String> FILENAME_TIME_FORMAT =
            ConfigOption.key("filename_time_format")
                    .stringType()
                    .defaultValue(DateUtils.Formatter.YYYY_MM_DD_SPOT.getValue())
                    .withDescription(
                            "Only used when `custom_filename` is true. The time format of the path");

    public static final ConfigEntry<FileFormat> FILE_FORMAT_TYPE =
            ConfigOption.key("file_format_type")
                    .enumType(FileFormat.class)
                    .defaultValue(FileFormat.CSV)
                    .withDescription("File format type, e.g. csv, orc, parquet, text");

    public static final ConfigEntry<String> ENCODING =
            ConfigOption.key("encoding")
                    .stringType()
                    .defaultValue("UTF-8")
                    .withDescription("The encoding of output file, e.g. UTF-8, ISO-8859-1....");

    public static final ConfigEntry<List<String>> SINK_COLUMNS =
            ConfigOption.key("sink_columns")
                    .listType()
                    .noDefaultValue()
                    .withDescription("Which columns need be wrote to file");

    public static final ConfigEntry<Boolean> IS_ENABLE_TRANSACTION =
            ConfigOption.key("is_enable_transaction")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("If or not enable transaction");

    public static final ConfigEntry<Integer> BATCH_SIZE =
            ConfigOption.key("batch_size")
                    .intType()
                    .defaultValue(DEFAULT_BATCH_SIZE)
                    .withDescription("The batch size of each split file");

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

    public static final ConfigEntry<Integer> MAX_ROWS_IN_MEMORY =
            ConfigOption.key("max_rows_in_memory")
                    .intType()
                    .noDefaultValue()
                    .withDescription("Max rows in memory,only valid for excel files");

    public static final ConfigEntry<String> SHEET_NAME =
            ConfigOption.key("sheet_name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("To be written sheet name,only valid for excel files");

    public static final ConfigEntry<String> XML_ROOT_TAG =
            ConfigOption.key("xml_root_tag")
                    .stringType()
                    .defaultValue("RECORDS")
                    .withDescription(
                            "Specifies the tag name of the root element within the XML file, only valid for xml files, default value is 'RECORDS'");

    public static final ConfigEntry<String> XML_ROW_TAG =
            ConfigOption.key("xml_row_tag")
                    .stringType()
                    .defaultValue("RECORD")
                    .withDescription(
                            "Specifies the tag name of the data rows within the XML file, only valid for xml files, default value is 'RECORD'");

    public static final ConfigEntry<Boolean> XML_USE_ATTR_FORMAT =
            ConfigOption.key("xml_use_attr_format")
                    .booleanType()
                    .noDefaultValue()
                    .withDescription(
                            "Specifies whether to process data using the tag attribute format, only valid for XML files.");

    public static final ConfigEntry<Boolean> ENABLE_HEADER_WRITE =
            ConfigOption.key("enable_header_write")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("false:dont write header,true:write header");

    public static final ConfigEntry<Boolean> PARQUET_AVRO_WRITE_TIMESTAMP_AS_INT96 =
            ConfigOption.key("parquet_avro_write_timestamp_as_int96")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            "Support writing Parquet INT96 from a timestamp, only valid for parquet files.");

    public static final ConfigEntry<List<String>> PARQUET_AVRO_WRITE_FIXED_AS_INT96 =
            ConfigOption.key("parquet_avro_write_fixed_as_int96")
                    .listType(String.class)
                    .defaultValue(Collections.emptyList())
                    .withDescription(
                            "Support writing Parquet INT96 from a 12-byte field, only valid for parquet files.");

    public static final ConfigEntry<SchemaSaveMode> SCHEMA_SAVE_MODE =
            ConfigOption.key("schema_save_mode")
                    .enumType(SchemaSaveMode.class)
                    .defaultValue(SchemaSaveMode.CREATE_SCHEMA_WHEN_NOT_EXIST)
                    .withDescription(
                            "Before the synchronization task begins, process the existing path");

    public static final ConfigEntry<DataSaveMode> DATA_SAVE_MODE =
            ConfigOption.key("data_save_mode")
                    .singleChoice(
                            DataSaveMode.class,
                            Arrays.asList(DROP_DATA, APPEND_DATA, ERROR_WHEN_DATA_EXISTS))
                    .defaultValue(APPEND_DATA)
                    .withDescription(
                            "Before the synchronization task begins, different processing of data files that already exist in the directory");

    public static final ConfigEntry<CsvStringQuoteMode> CSV_STRING_QUOTE_MODE =
            ConfigOption.key("csv_string_quote_mode")
                    .enumType(CsvStringQuoteMode.class)
                    .defaultValue(CsvStringQuoteMode.MINIMAL)
                    .withDescription("CSV file string quote mode, only valid for csv files");

    public static final ConfigEntry<String> KERBEROS_PRINCIPAL =
            ConfigOption.key("kerberos_principal")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("When use kerberos, we should set kerberos user principal");

    public static final ConfigEntry<String> KRB5_PATH =
            ConfigOption.key("krb5_path")
                    .stringType()
                    .defaultValue("/etc/krb5.conf")
                    .withDescription(
                            "When use kerberos, we should set krb5 path file path such as '/seatunnel/krb5.conf' or use the default path '/etc/krb5.conf'");

    public static final ConfigEntry<String> KERBEROS_KEYTAB_PATH =
            ConfigOption.key("kerberos_keytab_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("When using kerberos, We should specify the keytab path");
}
