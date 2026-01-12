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

package org.apache.seatunnel.config.sql.parser;

import org.apache.seatunnel.common.utils.ParserException;
import org.apache.seatunnel.config.sql.model.BaseConfig;
import org.apache.seatunnel.config.sql.model.Option;
import org.apache.seatunnel.config.sql.model.SeaTunnelConfig;
import org.apache.seatunnel.config.sql.model.SinkConfig;
import org.apache.seatunnel.config.sql.model.SourceConfig;
import org.apache.seatunnel.config.sql.model.TransformConfig;
import org.apache.seatunnel.config.sql.spi.SqlParserSpi;

import org.apache.commons.lang3.StringUtils;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_PLUGIN_OUTPUT_KEY;
import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_TABLE_CONNECTOR_KEY;
import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_TABLE_TYPE_KEY;
import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_TABLE_TYPE_SINK;
import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_TABLE_TYPE_SOURCE;

public class TableParser implements SqlParserSpi {
    @Override
    public boolean parse(
            Statement statement,
            Map<String, BaseConfig> sqlTables,
            SeaTunnelConfig seaTunnelConfig) {
        if (!(statement instanceof CreateTable)) {
            return false;
        }
        CreateTable createTable = (CreateTable) statement;

        // Handle CREATE TABLE ... AS SELECT (CTAS) logic
        if (createTable.getSelect() != null) {
            return parseCreateAsSql(createTable, sqlTables, seaTunnelConfig);
        }

        if (createTable.getTableOptionsStrings() == null) {
            return false;
        }

        Map<String, String> optionsMap = parseOptions(createTable);
        String tableName = createTable.getTable().getName();
        if (sqlTables.containsKey(tableName)) {
            throw new ParserException(String.format("Table name duplicate: %s", tableName));
        }

        String type = optionsMap.get(OPTION_TABLE_TYPE_KEY);
        if (OPTION_TABLE_TYPE_SOURCE.equalsIgnoreCase(type)) {
            SourceConfig sourceConfig = parseSourceSql(createTable, optionsMap);
            sqlTables.put(tableName, sourceConfig);
            seaTunnelConfig.getSourceConfigs().add(sourceConfig);
        } else if (OPTION_TABLE_TYPE_SINK.equalsIgnoreCase(type)) {
            SinkConfig sinkConfig = parseSinkSql(optionsMap);
            sqlTables.put(tableName, sinkConfig);
            seaTunnelConfig.getSinkConfigs().add(sinkConfig);
        }
        return true;
    }

    private boolean parseCreateAsSql(
            CreateTable createTable,
            Map<String, BaseConfig> sqlTables,
            SeaTunnelConfig seaTunnelConfig) {
        Select select = createTable.getSelect();
        TransformConfig transformConfig = new TransformConfig();
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        net.sf.jsqlparser.schema.Table table =
                (net.sf.jsqlparser.schema.Table) plainSelect.getFromItem();
        String pluginInputIdentifier = table.getName();

        if (!sqlTables.containsKey(pluginInputIdentifier)) {
            throw new ParserException(
                    String.format("The source table[%s] is not found", pluginInputIdentifier));
        }

        String pluginOutputIdentifier = createTable.getTable().getName();
        if (sqlTables.containsKey(pluginOutputIdentifier)) {
            throw new ParserException(
                    String.format("Table name duplicate: %s", pluginOutputIdentifier));
        }
        sqlTables.put(pluginOutputIdentifier, transformConfig);

        String query = select.toString();
        transformConfig.setPluginInputIdentifier(pluginInputIdentifier);
        transformConfig.setPluginOutputIdentifier(pluginOutputIdentifier);
        transformConfig.setQuery(query);
        seaTunnelConfig.getTransformConfigs().add(transformConfig);
        return true;
    }

    private SourceConfig parseSourceSql(CreateTable createTable, Map<String, String> options) {
        String connector = options.get(OPTION_TABLE_CONNECTOR_KEY);
        if (StringUtils.isEmpty(connector)) {
            throw new ParserException("The connector of option is none");
        }
        SourceConfig sourceConfig = new SourceConfig();
        sourceConfig.setConnector(connector);

        String pluginOutputIdentifier = createTable.getTable().getName();
        sourceConfig.setPluginOutputIdentifier(pluginOutputIdentifier);
        convertOptions(options, sourceConfig.getOptions());
        sourceConfig.getOptions().add(Option.of(OPTION_PLUGIN_OUTPUT_KEY, pluginOutputIdentifier));
        return sourceConfig;
    }

    private SinkConfig parseSinkSql(Map<String, String> options) {
        String connector = options.get(OPTION_TABLE_CONNECTOR_KEY);
        if (StringUtils.isEmpty(connector)) {
            throw new ParserException("The connector of option is none");
        }
        SinkConfig sinkConfig = new SinkConfig();
        sinkConfig.setConnector(connector);
        convertOptions(options, sinkConfig.getOptions());
        return sinkConfig;
    }

    private Map<String, String> parseOptions(CreateTable createTable) {
        // JSqlParser returns the full OPTIONS string list including WITH and brackets?
        // Usually getTableOptionsStrings return something like ["WITH", "( ... )"]
        // We just need to join them if multiple, or find the one with paren.
        // Assuming SeaTunnel SQL syntax is WITH (...)
        // The list might be null, but we checked earlier.

        // Robust strategy: Find standard WITH (...) pattern
        if (createTable.getTableOptionsStrings() == null
                || createTable.getTableOptionsStrings().isEmpty()) {
            return new LinkedHashMap<>();
        }

        // Concatenate all parts to be sure (jsqlparser might split by space)
        String fullOptions = String.join(" ", createTable.getTableOptionsStrings());
        // find WITH
        int withIdx = fullOptions.toUpperCase().indexOf("WITH");
        if (withIdx == -1) {
            return new LinkedHashMap<>();
        }
        String optionsBody = fullOptions.substring(withIdx + 4).trim();
        return org.apache.seatunnel.config.sql.utils.OptionUtils.parseOptions(optionsBody);
    }

    private void convertOptions(Map<String, String> options, Collection<Option> optionList) {
        options.forEach(
                (k, v) -> {
                    if (OPTION_TABLE_CONNECTOR_KEY.equalsIgnoreCase(k)
                            || OPTION_TABLE_TYPE_KEY.equalsIgnoreCase(k)
                            || OPTION_PLUGIN_OUTPUT_KEY.equalsIgnoreCase(k)) {
                        return;
                    }
                    optionList.add(Option.of(k, v));
                });
    }
}
