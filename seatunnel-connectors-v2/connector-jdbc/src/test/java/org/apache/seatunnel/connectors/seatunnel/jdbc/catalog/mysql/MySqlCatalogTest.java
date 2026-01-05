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

package org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.mysql;

import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.common.utils.JdbcUrlUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Please Test it in your local environment")
class MySqlCatalogTest {

    static MySqlCatalog mySqlCatalog;
    static JdbcUrlUtil.UrlInfo MysqlUrlInfo =
            JdbcUrlUtil.getUrlInfo(
                    "jdbc:mysql://127.0.0.1:3306/test?useSSL=false&allowPublicKeyRetrieval=true");
    static TablePath tablePathMySql;
    static String databaseName = "test";

    @Test
    void listDatabases() {}

    @Test
    void listTables() {}

    @Test
    void getColumnsDefaultValue() {}

    @BeforeAll
    static void before() {
        tablePathMySql = TablePath.of(databaseName, "mysql_to_mysql");
        mySqlCatalog = new MySqlCatalog("mysql", "root", "123456", MysqlUrlInfo, null);
        mySqlCatalog.open();
    }

    @Test
    void exists() {
        Assertions.assertTrue(mySqlCatalog.databaseExists("test"));
        Assertions.assertTrue(mySqlCatalog.tableExists(TablePath.of("test", "MY_TABLE")));
        Assertions.assertTrue(mySqlCatalog.tableExists(TablePath.of("test", "my_table")));
        Assertions.assertFalse(mySqlCatalog.tableExists(TablePath.of("test", "test")));
        Assertions.assertFalse(mySqlCatalog.databaseExists("mysql"));
    }

    @Test
    @Order(1)
    void getTable() {}

    @Test
    @Order(2)
    void createTableInternal() {}

    @Disabled
    // Manually dropping tables
    @Test
    void dropTableInternal() {}

    @Test
    void createDatabaseInternal() {}

    @Test
    void dropDatabaseInternal() {}

    @AfterAll
    static void after() {
        mySqlCatalog.close();
    }
}
