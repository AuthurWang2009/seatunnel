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

package org.apache.seatunnel.connectors.seatunnel.jdbc.catalog;

import org.apache.seatunnel.api.table.type.MultipleRowType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.common.exception.SeaTunnelRuntimeException;
import org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.mysql.MysqlDataTypeConvertor;
import org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.oracle.OracleDataTypeConvertor;
import org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.tidb.TiDBDataTypeConvertor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.mysql.cj.MysqlType.UNKNOWN;

public class DataTypeConvertorTest {

    @Test
    void testConvertorErrorMsgWithUnsupportedType() {
        SeaTunnelRowType rowType = new SeaTunnelRowType(new String[0], new SeaTunnelDataType[0]);
        MultipleRowType multipleRowType =
                new MultipleRowType(new String[] {"table"}, new SeaTunnelRowType[] {rowType});

        MysqlDataTypeConvertor mysql = new MysqlDataTypeConvertor();
        SeaTunnelRuntimeException exception4 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> mysql.toSeaTunnelType("test", "UNSUPPORTED_TYPE"));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-17], ErrorDescription:['MySQL' unsupported convert type 'UNKNOWN' of 'test' to SeaTunnel data type.]",
                exception4.getMessage());
        SeaTunnelRuntimeException exception5 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> mysql.toSeaTunnelType("test", UNKNOWN, new HashMap<>()));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-17], ErrorDescription:['MySQL' unsupported convert type 'UNKNOWN' of 'test' to SeaTunnel data type.]",
                exception5.getMessage());
        SeaTunnelRuntimeException exception6 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> mysql.toConnectorType("test", multipleRowType, new HashMap<>()));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-19], ErrorDescription:['MySQL' unsupported convert SeaTunnel data type 'MULTIPLE_ROW' of 'test' to connector data type.]",
                exception6.getMessage());

        OracleDataTypeConvertor oracle = new OracleDataTypeConvertor();
        SeaTunnelRuntimeException exception7 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> oracle.toSeaTunnelType("test", "UNSUPPORTED_TYPE"));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-17], ErrorDescription:['Oracle' unsupported convert type 'UNSUPPORTED_TYPE' of 'test' to SeaTunnel data type.]",
                exception7.getMessage());
        SeaTunnelRuntimeException exception8 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> oracle.toSeaTunnelType("test", "UNSUPPORTED_TYPE", new HashMap<>()));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-17], ErrorDescription:['Oracle' unsupported convert type 'UNSUPPORTED_TYPE' of 'test' to SeaTunnel data type.]",
                exception8.getMessage());
        SeaTunnelRuntimeException exception9 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> oracle.toConnectorType("test", multipleRowType, new HashMap<>()));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-19], ErrorDescription:['Oracle' unsupported convert SeaTunnel data type 'MULTIPLE_ROW' of 'test' to connector data type.]",
                exception9.getMessage());

        TiDBDataTypeConvertor tidb = new TiDBDataTypeConvertor();
        SeaTunnelRuntimeException exception22 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> tidb.toSeaTunnelType("test", "UNSUPPORTED_TYPE"));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-17], ErrorDescription:['TiDB' unsupported convert type 'UNKNOWN' of 'test' to SeaTunnel data type.]",
                exception22.getMessage());
        SeaTunnelRuntimeException exception23 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> tidb.toSeaTunnelType("test", UNKNOWN, new HashMap<>()));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-17], ErrorDescription:['TiDB' unsupported convert type 'UNKNOWN' of 'test' to SeaTunnel data type.]",
                exception23.getMessage());
        SeaTunnelRuntimeException exception24 =
                Assertions.assertThrows(
                        SeaTunnelRuntimeException.class,
                        () -> tidb.toConnectorType("test", multipleRowType, new HashMap<>()));
        Assertions.assertEquals(
                "ErrorCode:[COMMON-19], ErrorDescription:['TiDB' unsupported convert SeaTunnel data type 'MULTIPLE_ROW' of 'test' to connector data type.]",
                exception24.getMessage());
    }
}
