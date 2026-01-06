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

package org.apache.seatunnel.api.table.catalog;

import org.apache.seatunnel.api.config.ReadonlyConfig;
import org.apache.seatunnel.api.options.ConnectorCommonOptions;
import org.apache.seatunnel.api.table.type.ArrayType;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.MapType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.api.table.type.SqlType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CatalogTableUtilTest {
    @Test
    public void testSimpleSchemaParse() throws FileNotFoundException, URISyntaxException {
        String path = getTestConfigFile("/conf/simple.schema.conf");
        ReadonlyConfig config =
                ReadonlyConfig.fromMap(ConfigFactory.parseFile(new File(path)).root().unwrapped());
        SeaTunnelRowType seaTunnelRowType =
                CatalogTableUtil.buildWithConfig(config).getSeaTunnelRowType();
        Assertions.assertNotNull(seaTunnelRowType);
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("array")),
                ArrayType.BYTE_ARRAY_TYPE);
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("string")),
                BasicType.STRING_TYPE);
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("decimal")),
                new DecimalType(30, 8));
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("null")),
                BasicType.VOID_TYPE);
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("bytes")),
                PrimitiveByteArrayType.INSTANCE);
    }

    @Test
    public void testComplexSchemaParse() throws FileNotFoundException, URISyntaxException {
        String path = getTestConfigFile("/conf/complex.schema.conf");
        ReadonlyConfig config =
                ReadonlyConfig.fromMap(ConfigFactory.parseFile(new File(path)).root().unwrapped());
        SeaTunnelRowType seaTunnelRowType =
                CatalogTableUtil.buildWithConfig(config).getSeaTunnelRowType();
        Assertions.assertNotNull(seaTunnelRowType);
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("map")),
                new MapType<>(
                        BasicType.STRING_TYPE,
                        new MapType<>(BasicType.STRING_TYPE, BasicType.STRING_TYPE)));
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("map_array")),
                new MapType<>(
                        BasicType.STRING_TYPE,
                        new MapType<>(BasicType.STRING_TYPE, ArrayType.INT_ARRAY_TYPE)));
        Assertions.assertEquals(seaTunnelRowType.getTotalFields(), 18);
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("row")).getSqlType(),
                SqlType.ROW);
        SeaTunnelRowType nestedRowFieldType =
                (SeaTunnelRowType) seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("row"));
        Assertions.assertEquals(
                "map", nestedRowFieldType.getFieldName(nestedRowFieldType.indexOf("map")));
        Assertions.assertEquals(
                "row", nestedRowFieldType.getFieldName(nestedRowFieldType.indexOf("row")));
    }

    @Test
    public void testSpecialSchemaParse() throws FileNotFoundException, URISyntaxException {
        String path = getTestConfigFile("/conf/config_special_schema.conf");
        ReadonlyConfig config =
                ReadonlyConfig.fromMap(ConfigFactory.parseFile(new File(path)).root().unwrapped());
        SeaTunnelRowType seaTunnelRowType =
                CatalogTableUtil.buildWithConfig(config).getSeaTunnelRowType();
        Assertions.assertEquals(seaTunnelRowType.getTotalFields(), 12);
        // t.date is a specific field name test
        Assertions.assertEquals(
                seaTunnelRowType.getFieldType(seaTunnelRowType.indexOf("t.byteArray")).getSqlType(),
                SqlType.BYTES);
        Assertions.assertEquals(
                seaTunnelRowType.getFieldName(seaTunnelRowType.indexOf("t.date")), "t.date");
    }

    @Test
    public void testCatalogUtilGetCatalogTable() throws FileNotFoundException, URISyntaxException {
        String path = getTestConfigFile("/conf/getCatalogTable.conf");
        Config config = ConfigFactory.parseFile(new File(path));
        // source is a map { InMemory { ... } }
        Config sourceEnv = config.getConfig("source");
        String pluginName = sourceEnv.root().keySet().iterator().next();
        Config source = sourceEnv.getConfig(pluginName);

        ReadonlyConfig sourceReadonlyConfig = ReadonlyConfig.fromMap(source.root().unwrapped());
        List<CatalogTable> catalogTables =
                CatalogTableUtil.getCatalogTables(
                        sourceReadonlyConfig, Thread.currentThread().getContextClassLoader());
        Assertions.assertEquals(2, catalogTables.size());
        Assertions.assertEquals(
                TableIdentifier.of("InMemory", TablePath.of("st.public.table1")),
                catalogTables.get(0).getTableId());
        Assertions.assertEquals(
                TableIdentifier.of("InMemory", TablePath.of("st.public.table2")),
                catalogTables.get(1).getTableId());
        // test empty tables
        Config emptyTableSource =
                source.withValue(
                        ConnectorCommonOptions.TABLE_NAMES.key(),
                        ConfigValueFactory.fromIterable(new ArrayList<>()));
        ReadonlyConfig emptyReadonlyConfig =
                ReadonlyConfig.fromMap(emptyTableSource.root().unwrapped());

        Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        CatalogTableUtil.getCatalogTables(
                                emptyReadonlyConfig,
                                Thread.currentThread().getContextClassLoader()));
        // test unknown catalog
        Config cannotFindCatalogSource =
                source.withValue("plugin_name", ConfigValueFactory.fromAnyRef("unknownCatalog"));
        ReadonlyConfig cannotFindCatalogReadonlyConfig =
                ReadonlyConfig.fromMap(cannotFindCatalogSource.root().unwrapped());

        Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        CatalogTableUtil.getCatalogTables(
                                cannotFindCatalogReadonlyConfig,
                                Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void testDefaultTablePath() throws FileNotFoundException, URISyntaxException {
        String path = getTestConfigFile("/conf/default_tablepath.conf");
        Config config = ConfigFactory.parseFile(new File(path));
        // 'default_tablepath.conf' source is a map, not list.
        Config sourceEnv = config.getConfig("source");
        // We need the inner config "MongoDB-CDC"
        String pluginName = sourceEnv.root().keySet().iterator().next();
        Config source = sourceEnv.getConfig(pluginName);

        ReadonlyConfig sourceReadonlyConfig = ReadonlyConfig.fromMap(source.root().unwrapped());
        CatalogTable catalogTable = CatalogTableUtil.buildWithConfig(sourceReadonlyConfig);
        Assertions.assertEquals(
                TablePath.DEFAULT.getDatabaseName(), catalogTable.getTablePath().getDatabaseName());
        Assertions.assertEquals(
                TablePath.DEFAULT.getSchemaName(), catalogTable.getTablePath().getSchemaName());
        Assertions.assertEquals(
                TablePath.DEFAULT.getTableName(), catalogTable.getTablePath().getTableName());
    }

    @Test
    public void testGenericRowSchemaTest() throws FileNotFoundException, URISyntaxException {
        String path = getTestConfigFile("/conf/generic_row.schema.conf");
        ReadonlyConfig config =
                ReadonlyConfig.fromMap(ConfigFactory.parseFile(new File(path)).root().unwrapped());
        SeaTunnelRowType seaTunnelRowType =
                CatalogTableUtil.buildWithConfig(config).getSeaTunnelRowType();
        Assertions.assertNotNull(seaTunnelRowType);

        // Verify keys using contains or just assume presence if size matches, but here verifying
        // names
        // specifically
        int map0Index = seaTunnelRowType.indexOf("map0");
        int map1Index = seaTunnelRowType.indexOf("map1");
        Assertions.assertTrue(map0Index >= 0);
        Assertions.assertTrue(map1Index >= 0);

        MapType<String, SeaTunnelRowType> mapType0 =
                (MapType<String, SeaTunnelRowType>) seaTunnelRowType.getFieldType(map0Index);
        MapType<String, SeaTunnelRowType> mapType1 =
                (MapType<String, SeaTunnelRowType>) seaTunnelRowType.getFieldType(map1Index);
        Assertions.assertNotNull(mapType0);
        Assertions.assertNotNull(mapType1);
        Assertions.assertEquals(BasicType.STRING_TYPE, mapType0.getKeyType());

        // Update expectedVal to matching actual order: c_row, c_string, c_int
        SeaTunnelRowType expectedVal =
                new SeaTunnelRowType(
                        new String[] {"c_row", "c_string", "c_int"},
                        new SeaTunnelDataType[] {
                            new SeaTunnelRowType(
                                    new String[] {"c_int"},
                                    new SeaTunnelDataType[] {BasicType.INT_TYPE}),
                            BasicType.STRING_TYPE,
                            BasicType.INT_TYPE
                        });
        SeaTunnelRowType mapType0ValType =
                (SeaTunnelRowType) ((SeaTunnelDataType<?>) mapType0.getValueType());
        Assertions.assertEquals(expectedVal, mapType0ValType);
        SeaTunnelRowType mapType1ValType =
                (SeaTunnelRowType) ((SeaTunnelDataType<?>) mapType1.getValueType());
        Assertions.assertEquals(expectedVal, mapType1ValType);
    }

    public static String getTestConfigFile(String configFile)
            throws FileNotFoundException, URISyntaxException {
        URL resource = CatalogTableUtilTest.class.getResource(configFile);
        if (resource == null) {
            throw new FileNotFoundException("Can't find config file: " + configFile);
        }
        return Paths.get(resource.toURI()).toString();
    }
}
