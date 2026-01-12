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
import org.apache.seatunnel.config.sql.model.TransformConfig;
import org.apache.seatunnel.config.sql.spi.SqlParserSpi;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_PLUGIN_INPUT_KEY;
import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_TABLE_TYPE_SINK;
import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_TABLE_TYPE_SOURCE;
import static org.apache.seatunnel.config.sql.utils.Constant.OPTION_TABLE_TYPE_TRANSFORM;
import static org.apache.seatunnel.config.sql.utils.Constant.TEMP_TABLE_SUFFIX;

public class InsertParser implements SqlParserSpi {
    private final AtomicInteger tempTableIndex;

    public InsertParser(AtomicInteger tempTableIndex) {
        this.tempTableIndex = tempTableIndex;
    }

    @Override
    public boolean parse(
            Statement statement,
            Map<String, BaseConfig> sqlTables,
            SeaTunnelConfig seaTunnelConfig) {
        if (!(statement instanceof Insert)) {
            return false;
        }
        Insert insertSql = (Insert) statement;

        if (insertSql.getColumns() != null && !insertSql.getColumns().isEmpty()) {
            throw new ParserException("Insert sql must not have columns");
        }
        Select select = insertSql.getSelect();
        if (select == null
                || select.getSelectBody() == null
                || !(select.getSelectBody() instanceof PlainSelect)) {
            throw new ParserException("Insert sql must have select statement");
        }

        String targetTableName = insertSql.getTable().getName();
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        String pluginInputIdentifier;
        String pluginOutputIdentifier;
        if (plainSelect.getFromItem() == null) {
            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
            if (selectItems.size() != 1) {
                throw new ParserException("Source table must be specified in SQL: " + insertSql);
            }
            SelectItem<?> selectItem = selectItems.get(0);
            Column column = (Column) selectItem.getExpression();
            pluginInputIdentifier = column.getColumnName();
            pluginOutputIdentifier = pluginInputIdentifier;
        } else {
            if (!(plainSelect.getFromItem() instanceof Table)) {
                throw new ParserException("Unsupported syntax: " + insertSql);
            }
            Table table = (Table) plainSelect.getFromItem();
            pluginInputIdentifier = table.getName();
            pluginOutputIdentifier =
                    pluginInputIdentifier + TEMP_TABLE_SUFFIX + tempTableIndex.getAndIncrement();
            String query = select.toString();

            TransformConfig transformConfig = new TransformConfig();
            transformConfig.setPluginInputIdentifier(pluginInputIdentifier);
            transformConfig.setPluginOutputIdentifier(pluginOutputIdentifier);
            transformConfig.setQuery(query);
            seaTunnelConfig.getTransformConfigs().add(transformConfig);
        }

        if (!sqlTables.containsKey(pluginInputIdentifier)
                || (!OPTION_TABLE_TYPE_SOURCE.equalsIgnoreCase(
                                sqlTables.get(pluginInputIdentifier).getType())
                        && !OPTION_TABLE_TYPE_TRANSFORM.equalsIgnoreCase(
                                sqlTables.get(pluginInputIdentifier).getType()))) {
            throw new ParserException(
                    String.format("The source table[%s] is not found", pluginInputIdentifier));
        }
        if (!sqlTables.containsKey(targetTableName)
                || !OPTION_TABLE_TYPE_SINK.equalsIgnoreCase(
                        sqlTables.get(targetTableName).getType())) {
            throw new ParserException(
                    String.format("The sink table[%s] is not found", pluginInputIdentifier));
        }

        SinkConfig sinkConfig = (SinkConfig) sqlTables.get(targetTableName);
        SinkConfig sinkConfigNew = new SinkConfig();
        sinkConfigNew.setConnector(sinkConfig.getConnector());
        sinkConfigNew.setPluginInputIdentifier(pluginOutputIdentifier);
        sinkConfigNew.getOptions().addAll(sinkConfig.getOptions());
        sinkConfigNew.getOptions().add(Option.of(OPTION_PLUGIN_INPUT_KEY, pluginOutputIdentifier));

        seaTunnelConfig.getSinkConfigs().add(sinkConfigNew);
        return true;
    }
}
