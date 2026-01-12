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

package org.apache.seatunnel.api.table.catalog.schema;

import org.apache.seatunnel.api.config.Config;
import org.apache.seatunnel.api.config.TestConfigUtils;
import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.ConstraintKey;
import org.apache.seatunnel.api.table.catalog.PrimaryKey;
import org.apache.seatunnel.api.table.catalog.TableSchema;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.api.table.type.SqlType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ReadonlyConfigParserTest {

    @Test
    void parseColumn() {
        Config config = TestConfigUtils.getSchemaColumnConfig();

        ReadonlyConfigParser readonlyConfigParser = new ReadonlyConfigParser();
        TableSchema tableSchema = readonlyConfigParser.parse(config);
        assertPrimaryKey(tableSchema);
        assertConstraintKey(tableSchema);
        assertColumn(tableSchema, true);
    }

    @Test
    void parseField() {
        Config config = TestConfigUtils.getSchemaFieldConfig();

        ReadonlyConfigParser readonlyConfigParser = new ReadonlyConfigParser();
        TableSchema tableSchema = readonlyConfigParser.parse(config);
        assertPrimaryKey(tableSchema);
        assertConstraintKey(tableSchema);
        assertColumn(tableSchema, false);
    }

    private void assertPrimaryKey(TableSchema tableSchema) {
        PrimaryKey primaryKey = tableSchema.getPrimaryKey();
        Assertions.assertEquals("id", primaryKey.getPrimaryKey());
        Assertions.assertEquals("id", primaryKey.getColumnNames().get(0));
    }

    private void assertConstraintKey(TableSchema tableSchema) {
        List<ConstraintKey> constraintKeys = tableSchema.getConstraintKeys();
        ConstraintKey constraintKey = constraintKeys.get(0);
        Assertions.assertEquals("id_index", constraintKey.getConstraintName());
        Assertions.assertEquals(
                ConstraintKey.ConstraintType.INDEX_KEY, constraintKey.getConstraintType());
        Assertions.assertEquals("id", constraintKey.getColumnNames().get(0).getColumnName());
        Assertions.assertEquals(
                ConstraintKey.ColumnSortType.ASC,
                constraintKey.getColumnNames().get(0).getSortType());
    }

    private void assertColumn(TableSchema tableSchema, boolean comeFromColumnConfig) {
        List<Column> columns = tableSchema.getColumns();
        Assertions.assertEquals(19, columns.size());

        Column idColumn = findColumn(columns, "id");
        Assertions.assertEquals("id", idColumn.getName());

        Column mapColumn = findColumn(columns, "map");
        Assertions.assertEquals(
                "map<string, map<string, string>>",
                mapColumn.getDataType().toString().toLowerCase());

        Column mapArrayColumn = findColumn(columns, "map_array");
        Assertions.assertEquals(
                "map<string, map<string, array<int>>>",
                mapArrayColumn.getDataType().toString().toLowerCase());

        Column arrayColumn = findColumn(columns, "array");
        Assertions.assertEquals(
                "array<tinyint>", arrayColumn.getDataType().toString().toLowerCase());

        Column stringColumn = findColumn(columns, "string");
        Assertions.assertEquals("string", stringColumn.getDataType().toString().toLowerCase());

        Column rowColumn = findColumn(columns, "row");
        Assertions.assertEquals(SqlType.ROW, rowColumn.getDataType().getSqlType());

        SeaTunnelRowType seaTunnelRowType = (SeaTunnelRowType) rowColumn.getDataType();
        Assertions.assertEquals(18, seaTunnelRowType.getTotalFields());

        int fieldIndex = seaTunnelRowType.indexOf("row");
        SeaTunnelRowType seatunnalRowType1 =
                (SeaTunnelRowType) seaTunnelRowType.getFieldType(fieldIndex);
        Assertions.assertEquals(17, seatunnalRowType1.getTotalFields());

        if (comeFromColumnConfig) {
            Assertions.assertEquals(0, idColumn.getDefaultValue());
            Assertions.assertEquals("I'm default value", stringColumn.getDefaultValue());
            Assertions.assertEquals(false, findColumn(columns, "boolean").getDefaultValue());
            Assertions.assertEquals(1.1, findColumn(columns, "float").getDefaultValue());
            Assertions.assertEquals("2020-01-01", findColumn(columns, "date").getDefaultValue());
            Assertions.assertEquals(4294967295L, stringColumn.getColumnLength());
        }
    }

    private Column findColumn(List<Column> columns, String name) {
        return columns.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Column not found: " + name));
    }
}
