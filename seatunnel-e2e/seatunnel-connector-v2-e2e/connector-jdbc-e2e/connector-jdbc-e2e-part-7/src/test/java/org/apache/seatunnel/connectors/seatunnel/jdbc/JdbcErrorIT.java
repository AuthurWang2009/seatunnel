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

package org.apache.seatunnel.connectors.seatunnel.jdbc;

import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.table.factory.TableSourceFactoryContext;
import org.apache.seatunnel.common.exception.SeaTunnelRuntimeException;
import org.apache.seatunnel.connectors.seatunnel.jdbc.source.JdbcSourceFactory;
import org.apache.seatunnel.e2e.common.TestResource;
import org.apache.seatunnel.e2e.common.TestSuiteBase;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerLoggerFactory;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.given;

/**
 * This test case is used to test that the jdbc connector returns the expected error when
 * encountering an unsupported data type. If a certain type is supported and the test case becomes
 * invalid, we need to find a replacement to allow the test case to continue to be executed, instead
 * of deleting it.
 */
@Slf4j
public class JdbcErrorIT extends TestSuiteBase implements TestResource {
    private static final String MYSQL_IMAGE = "mysql:8.0";
    private MySQLContainer<?> MYSQL_CONTAINER;
    private static final String MYSQL_SOURCE_DDL1 =
            "CREATE TABLE IF NOT EXISTS mysql_e2e_source_table1 (\n"
                    + "  gid INT AUTO_INCREMENT PRIMARY KEY,"
                    + "  geo1 POINT,"
                    + "  geo2 POINT\n"
                    + ")";
    private static final String MYSQL_SOURCE_DDL2 =
            "CREATE TABLE IF NOT EXISTS mysql_e2e_source_table2 (\n"
                    + "  gid INT AUTO_INCREMENT PRIMARY KEY,"
                    + "  str VARCHAR(255),"
                    + "  geo2 POINT\n"
                    + ")";
    private static final String MYSQL_SOURCE_DDL3 =
            "CREATE TABLE IF NOT EXISTS mysql_e2e_source_table3 (\n"
                    + "  gid INT AUTO_INCREMENT PRIMARY KEY,"
                    + "  str1 VARCHAR(255),"
                    + "  str2 VARCHAR(255)\n"
                    + ")";

    @BeforeAll
    @Override
    public void startUp() throws Exception {
        MYSQL_CONTAINER =
                new MySQLContainer<>(MYSQL_IMAGE)
                        .withNetwork(TestSuiteBase.NETWORK)
                        .withNetworkAliases("mysql")
                        .withDatabaseName("seatunnel")
                        .withUsername("root")
                        .withLogConsumer(
                                new Slf4jLogConsumer(DockerLoggerFactory.getLogger(MYSQL_IMAGE)));
        Startables.deepStart(Stream.of(MYSQL_CONTAINER)).join();
        log.info("MySQL container started");
        Class.forName(MYSQL_CONTAINER.getDriverClassName());
        given().ignoreExceptions()
                .await()
                .atLeast(100, TimeUnit.MILLISECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .atMost(2, TimeUnit.MINUTES)
                .untilAsserted(this::initializeJdbcTable);
        log.info("mysql data initialization succeeded. Procedure");
    }

    @Test
    void testThrowMultiTableAndFieldsInfoWhenDataTypeUnsupported() {
        ReadonlyConfig config =
                ReadonlyConfig.fromMap(
                        new HashMap<String, Object>() {
                            {
                                put("url", MYSQL_CONTAINER.getJdbcUrl());
                                put("driver", MYSQL_CONTAINER.getDriverClassName());
                                put("user", MYSQL_CONTAINER.getUsername());
                                put("password", MYSQL_CONTAINER.getPassword());
                                put(
                                        "table_list",
                                        new ArrayList<Map<String, Object>>() {
                                            {
                                                add(
                                                        new HashMap<String, Object>() {
                                                            {
                                                                put(
                                                                        "table_path",
                                                                        "seatunnel.mysql_e2e_source_table1");
                                                            }
                                                        });
                                                add(
                                                        new HashMap<String, Object>() {
                                                            {
                                                                put(
                                                                        "table_path",
                                                                        "seatunnel.mysql_e2e_source_table2");
                                                                put(
                                                                        "query",
                                                                        "select * from seatunnel.mysql_e2e_source_table2");
                                                            }
                                                        });
                                                add(
                                                        new HashMap<String, Object>() {
                                                            {
                                                                put(
                                                                        "table_path",
                                                                        "seatunnel.mysql_e2e_source_table3");
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
        TableSourceFactoryContext context =
                new TableSourceFactoryContext(
                        config, Thread.currentThread().getContextClassLoader());
        SeaTunnelRuntimeException exception =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> {
                            SeaTunnelSource source =
                                    new JdbcSourceFactory().createSource(context).createSource();
                            source.getProducedCatalogTables();
                        });
        Assertions.assertEquals(
                "ErrorCode:[COMMON-21], ErrorDescription:['MySQL' tables unsupported get catalog table，"
                        + "the corresponding field types in the following tables are not supported:"
                        + " '{\"seatunnel.mysql_e2e_source_table1\":{\"geo1\":\"POINT\",\"geo2\":\"POINT\"}}']",
                exception.getMessage());
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        result.put(
                "seatunnel.mysql_e2e_source_table1",
                new LinkedHashMap<String, String>() {
                    {
                        put("geo1", "POINT");
                        put("geo2", "POINT");
                    }
                });
        Assertions.assertEquals(result, exception.getParamsValueAs("tableUnsupportedTypes"));
    }

    private void initializeJdbcTable() {
        try (Connection connection = getJdbcConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(MYSQL_SOURCE_DDL1);
            statement.execute(MYSQL_SOURCE_DDL2);
            statement.execute(MYSQL_SOURCE_DDL3);
        } catch (SQLException e) {
            throw new RuntimeException("Initializing MySQL table failed!", e);
        }
    }

    private Connection getJdbcConnection() throws SQLException {
        return DriverManager.getConnection(
                MYSQL_CONTAINER.getJdbcUrl(),
                MYSQL_CONTAINER.getUsername(),
                MYSQL_CONTAINER.getPassword());
    }

    @AfterAll
    @Override
    public void tearDown() {
        if (MYSQL_CONTAINER != null) {
            MYSQL_CONTAINER.stop();
        }
    }
}
