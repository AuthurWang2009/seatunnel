/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.seatunnel.config.sql;

import org.apache.seatunnel.api.config.Config;
import org.apache.seatunnel.common.utils.ParserException;
import org.apache.seatunnel.config.sql.model.BaseConfig;
import org.apache.seatunnel.config.sql.model.Option;
import org.apache.seatunnel.config.sql.model.SeaTunnelConfig;
import org.apache.seatunnel.config.sql.parser.InsertParser;
import org.apache.seatunnel.config.sql.parser.TableParser;
import org.apache.seatunnel.config.sql.spi.SqlParserSpi;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_PLUGIN_INPUT_KEY;
import static org.apache.seatunnel.config.sql.utils.Constant.SQL_ANNOTATION_PREFIX;
import static org.apache.seatunnel.config.sql.utils.Constant.SQL_ANNOTATION_PREFIX2;
import static org.apache.seatunnel.config.sql.utils.Constant.SQL_ANNOTATION_SUFFIX;
import static org.apache.seatunnel.config.sql.utils.Constant.SQL_CONFIG_ANNOTATION_PREFIX;
import static org.apache.seatunnel.config.sql.utils.Constant.SQL_DELIMITER;

@Slf4j
public class SqlConfigBuilder {

    public static Config of(@NonNull Path sqlFilePath) {
        try {
            List<String> lines = Files.readAllLines(sqlFilePath);
            return of(lines);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse job config file: " + sqlFilePath, e);
        }
    }

    public static Config of(@NonNull String sqlContent) {
        try {
            List<String> lines = new ArrayList<>();
            String[] lineArray = sqlContent.split("\\r?\\n");
            Collections.addAll(lines, lineArray);
            return of(lines);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse job config: ", e);
        }
    }

    private static Config of(@NonNull List<String> lines) {
        try {
            Map<String, BaseConfig> sqlTables = new LinkedHashMap<>();
            SeaTunnelConfig seaTunnelConfig = new SeaTunnelConfig();

            List<String> sqlLines = parseAnnoConfigAndSqlLine(lines, seaTunnelConfig);

            // Split SQL
            List<String> sqlList = split4SqlList(sqlLines);

            // Register Parsers
            AtomicInteger tempTableIndex = new AtomicInteger(1);
            List<SqlParserSpi> parsers =
                    Arrays.asList(new TableParser(), new InsertParser(tempTableIndex));

            // 1. First pass: Parse CREATE TABLE (Sources & Sinks) context
            // Some parsers like TableParser might need to run first to establish context
            for (Iterator<String> it = sqlList.iterator(); it.hasNext(); ) {
                String sql = it.next();
                Statement statement = CCJSqlParserUtil.parse(sql);

                // We prioritize TableParser execution for context building
                if (statement instanceof CreateTable) {
                    for (SqlParserSpi parser : parsers) {
                        if (parser.parse(statement, sqlTables, seaTunnelConfig)) {
                            it.remove();
                            break;
                        }
                    }
                }
            }

            // 2. Second pass: Parse remaining statements (Transformers, Inserts)
            for (String sql : sqlList) {
                Statement statement = CCJSqlParserUtil.parse(sql);
                boolean handled = false;
                for (SqlParserSpi parser : parsers) {
                    if (parser.parse(statement, sqlTables, seaTunnelConfig)) {
                        handled = true;
                        break;
                    }
                }
                if (!handled) {
                    throw new ParserException(
                            String.format("Unsupported SQL syntax: %s", statement));
                }
            }

            // filter out the sink config without 'plugin_input' option
            seaTunnelConfig.setSinkConfigs(
                    seaTunnelConfig.getSinkConfigs().stream()
                            .filter(
                                    sinkConfig -> {
                                        boolean containSourceTable = false;
                                        for (Option option : sinkConfig.getOptions()) {
                                            if (option.getKey().equals(OPTION_PLUGIN_INPUT_KEY)) {
                                                containSourceTable = true;
                                                break;
                                            }
                                        }
                                        return containSourceTable;
                                    })
                            .collect(Collectors.toList()));
            if (seaTunnelConfig.getSourceConfigs().isEmpty()) {
                throw new ParserException("The SQL config must contain at least one source table");
            }
            if (seaTunnelConfig.getSinkConfigs().isEmpty()) {
                throw new ParserException(
                        "The SQL config must contain `INSERT INTO ... SELECT ...` syntax");
            }

            // render to hocon config
            return ConfigTemplate.generate(seaTunnelConfig);
        } catch (ParserException e) {
            throw e;
        } catch (Exception e) {
            throw new ParserException(e);
        }
    }

    private static List<String> parseAnnoConfigAndSqlLine(
            List<String> lines, SeaTunnelConfig seaTunnelConfig) {
        List<String> sqlLines = new ArrayList<>();
        List<String> annotationConfigs = new ArrayList<>();
        boolean annoConfig = false;
        boolean anno = false;
        StringJoiner annotationConfig = new StringJoiner("\n");

        for (String line : lines) {
            if (line.trim().startsWith(SQL_ANNOTATION_PREFIX2)) {
                continue;
            }
            if (line.trim().equals(SQL_CONFIG_ANNOTATION_PREFIX)) {
                annoConfig = true;
                continue;
            }
            if (line.trim().startsWith(SQL_ANNOTATION_PREFIX)) {
                anno = true;
                continue;
            }
            if (anno) {
                if (line.trim().equals(SQL_ANNOTATION_SUFFIX)) {
                    anno = false;
                }
            } else if (annoConfig) {
                if (line.trim().equals(SQL_ANNOTATION_SUFFIX)) {
                    annoConfig = false;
                    annotationConfigs.add(annotationConfig.toString());
                    annotationConfig = new StringJoiner("\n");
                } else {
                    annotationConfig.add(line);
                }
            } else {
                if (StringUtils.isNotEmpty(line.trim())) {
                    sqlLines.add(line);
                }
            }
        }
        seaTunnelConfig.getEnvConfigs().addAll(annotationConfigs);
        return sqlLines;
    }

    private static List<String> split4SqlList(List<String> sqlLines) {
        List<String> sqlList = new ArrayList<>();
        StringJoiner sqlSj = new StringJoiner(" ");
        for (String line : sqlLines) {
            line = line.trim();
            int commentIdx = line.indexOf(" " + SQL_ANNOTATION_PREFIX2);
            if (commentIdx > -1) {
                line = line.substring(0, commentIdx);
            }
            if (line.endsWith(SQL_DELIMITER)) {
                line = line.substring(0, line.length() - 1);
                sqlSj.add(line);
                sqlList.add(sqlSj.toString());
                sqlSj = new StringJoiner(" ");
            } else {
                sqlSj.add(line);
            }
        }
        return sqlList;
    }
}
