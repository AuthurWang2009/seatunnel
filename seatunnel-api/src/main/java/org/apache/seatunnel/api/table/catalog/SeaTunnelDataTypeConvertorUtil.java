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

import org.apache.seatunnel.api.table.type.ArrayType;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.MapType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.api.table.type.SqlType;
import org.apache.seatunnel.api.table.type.VectorType;
import org.apache.seatunnel.common.exception.CommonError;
import org.apache.seatunnel.common.parser.ConfigParser;

import java.util.List;

public class SeaTunnelDataTypeConvertorUtil {

    /**
     * @param columnType column type, should be {@link SeaTunnelDataType##toString}.
     * @return {@link SeaTunnelDataType} instance.
     */
    public static SeaTunnelDataType<?> deserializeSeaTunnelDataType(
            String field, String columnType) {
        SqlType sqlType = null;
        try {
            String compatible = compatibleTypeDeclare(columnType);
            sqlType = SqlType.valueOf(compatible.toUpperCase().replace(" ", ""));
        } catch (IllegalArgumentException e) {
            // nothing
        }
        if (sqlType == null) {
            return parseComplexDataType(field, columnType);
        }
        switch (sqlType) {
            case STRING:
                return BasicType.STRING_TYPE;
            case BOOLEAN:
                return BasicType.BOOLEAN_TYPE;
            case TINYINT:
                return BasicType.BYTE_TYPE;
            case BYTES:
                return PrimitiveByteArrayType.INSTANCE;
            case SMALLINT:
                return BasicType.SHORT_TYPE;
            case INT:
                return BasicType.INT_TYPE;
            case BIGINT:
                return BasicType.LONG_TYPE;
            case FLOAT:
                return BasicType.FLOAT_TYPE;
            case DOUBLE:
                return BasicType.DOUBLE_TYPE;
            case NULL:
                return BasicType.VOID_TYPE;
            case DATE:
                return LocalTimeType.LOCAL_DATE_TYPE;
            case TIME:
                return LocalTimeType.LOCAL_TIME_TYPE;
            case TIMESTAMP:
                return LocalTimeType.LOCAL_DATE_TIME_TYPE;
            case TIMESTAMP_TZ:
                return LocalTimeType.OFFSET_DATE_TIME_TYPE;
            case MAP:
                return parseMapType(field, columnType);
            case BINARY_VECTOR:
                return VectorType.VECTOR_BINARY_TYPE;
            case FLOAT_VECTOR:
                return VectorType.VECTOR_FLOAT_TYPE;
            case FLOAT16_VECTOR:
                return VectorType.VECTOR_FLOAT16_TYPE;
            case BFLOAT16_VECTOR:
                return VectorType.VECTOR_BFLOAT16_TYPE;
            case SPARSE_FLOAT_VECTOR:
                return VectorType.VECTOR_SPARSE_FLOAT_TYPE;
            default:
                throw CommonError.unsupportedDataType("SeaTunnel", columnType, field);
        }
    }

    private static SeaTunnelDataType<?> parseComplexDataType(String field, String columnType) {
        String type = columnType.toUpperCase().trim();
        if (type.startsWith(SqlType.MAP.name())) {
            return parseMapType(field, columnType);
        }
        if (type.startsWith(SqlType.ARRAY.name())) {
            return parseArrayType(field, columnType);
        }
        if (type.startsWith(SqlType.DECIMAL.name())) {
            return parseDecimalType(columnType);
        }
        return parseRowType(columnType);
    }

    /**
     * User-facing data type declarations will adhere to the specifications outlined in
     * schema-feature.md. To maintain backward compatibility, this function will transform type
     * declarations into standard form, including: <code>long -> bigint</code>, <code>
     * short -> smallint</code>, and <code>byte -> tinyint</code>.
     *
     * <p>In a future version, user-facing data type declarations will strictly follow the
     * specifications, and this function will be removed.
     *
     * @param declare
     * @return compatible type
     */
    @Deprecated
    private static String compatibleTypeDeclare(String declare) {
        switch (declare.trim().toUpperCase()) {
            case "LONG":
                return "BIGINT";
            case "SHORT":
                return "SMALLINT";
            case "BYTE":
                return "TINYINT";
            default:
                return declare;
        }
    }

    private static final ConfigParser CONFIG_PARSER;

    static {
        ConfigParser hoconParser = null;
        for (ConfigParser parser : java.util.ServiceLoader.load(ConfigParser.class)) {
            if (parser.getConfigType()
                    == org.apache.seatunnel.common.config.ConfigType.HOCON) {
                hoconParser = parser;
                break;
            }
        }

        if (hoconParser == null) {
            throw new RuntimeException("HOCON ConfigParser not found");
        }
        CONFIG_PARSER = hoconParser;
    }

    private static java.util.Map<String, Object> parseConfig(String content) {
        return CONFIG_PARSER.parse(content);
    }

    private static SeaTunnelDataType<?> parseRowTypeFromList(String columnStr) {
        String confPayload = "{conf = " + columnStr + "}";
        java.util.Map<String, Object> conf;
        try {
            conf = parseConfig(confPayload);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    String.format("HOCON Config parse from %s failed.", confPayload), e);
        }
        return parseRowTypeFromList((List<Object>) conf.get("conf"));
    }

    private static SeaTunnelDataType<?> parseRowTypeFromList(List<Object> conf) {
        String[] fieldNames = new String[conf.size()];
        SeaTunnelDataType<?>[] fieldTypes = new SeaTunnelDataType[conf.size()];
        for (int i = 0; i < conf.size(); i++) {
            Object configValue = conf.get(i);
            if (!(configValue instanceof java.util.Map)) {
                throw new IllegalArgumentException(
                        "Unsupported parse SeaTunnel Type from " + configValue.toString());
            }
            java.util.Map<String, Object> config = (java.util.Map<String, Object>) configValue;
            String name = String.valueOf(config.get("name"));
            String type = String.valueOf(config.get("type"));
            fieldNames[i] = name;
            fieldTypes[i] = deserializeSeaTunnelDataType(name, type);
        }
        return new SeaTunnelRowType(fieldNames, fieldTypes);
    }

    private static SeaTunnelDataType<?> parseRowType(String columnStr) {
        String confPayload = "{conf = " + columnStr + "}";
        java.util.Map<String, Object> conf;
        try {
            conf = parseConfig(confPayload);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    String.format("HOCON Config parse from %s failed.", confPayload), e);
        }
        Object confValue = conf.get("conf");
        if (!(confValue instanceof java.util.Map)) {
            throw CommonError.unsupportedDataType("SeaTunnel", columnStr, "test");
        }
        return parseRowType((java.util.Map<String, Object>) confValue);
    }

    private static SeaTunnelDataType<?> parseRowType(java.util.Map<String, Object> conf) {
        String[] fieldNames = new String[conf.size()];
        SeaTunnelDataType<?>[] fieldTypes = new SeaTunnelDataType[conf.size()];
        conf.keySet().toArray(fieldNames);

        for (int idx = 0; idx < fieldNames.length; idx++) {
            String fieldName = fieldNames[idx];
            Object typeVal = conf.get(fieldName);
            if (typeVal instanceof String) {
                fieldTypes[idx] = deserializeSeaTunnelDataType(fieldNames[idx], (String) typeVal);
            } else if (typeVal instanceof java.util.Map) {
                fieldTypes[idx] = parseRowType((java.util.Map<String, Object>) typeVal);
            } else {
                throw new IllegalArgumentException(
                        String.format("Unsupported parse SeaTunnel Type from '%s'.", typeVal));
            }
        }
        return new SeaTunnelRowType(fieldNames, fieldTypes);
    }

    private static SeaTunnelDataType<?> parseMapType(String field, String columnStr) {
        String genericType = getGenericType(columnStr).trim();
        int index =
                genericType.toUpperCase().startsWith(SqlType.DECIMAL.name())
                        ?
                        // if map key is decimal, we should find the index of second ','
                        genericType.indexOf(",", genericType.indexOf(",") + 1)
                        :
                        // if map key is not decimal, we should find the index of first ','
                        genericType.indexOf(",");
        String keyGenericType = genericType.substring(0, index).trim();
        String valueGenericType = genericType.substring(index + 1).trim();
        return new MapType<>(
                deserializeSeaTunnelDataType(field, keyGenericType),
                deserializeSeaTunnelDataType(field, valueGenericType));
    }

    private static String getGenericType(String columnStr) {
        // get the content between '<' and '>'
        return columnStr.substring(columnStr.indexOf("<") + 1, columnStr.lastIndexOf(">"));
    }

    private static SeaTunnelDataType<?> parseArrayType(String field, String columnStr) {
        String genericType = getGenericType(columnStr).trim();
        SeaTunnelDataType<?> dataType = deserializeSeaTunnelDataType(field, genericType);
        switch (dataType.getSqlType()) {
            case STRING:
                return ArrayType.STRING_ARRAY_TYPE;
            case BOOLEAN:
                return ArrayType.BOOLEAN_ARRAY_TYPE;
            case TINYINT:
                return ArrayType.BYTE_ARRAY_TYPE;
            case SMALLINT:
                return ArrayType.SHORT_ARRAY_TYPE;
            case INT:
                return ArrayType.INT_ARRAY_TYPE;
            case BIGINT:
                return ArrayType.LONG_ARRAY_TYPE;
            case FLOAT:
                return ArrayType.FLOAT_ARRAY_TYPE;
            case DOUBLE:
                return ArrayType.DOUBLE_ARRAY_TYPE;
            case MAP:
                MapType<?, ?> mapType = (MapType<?, ?>) dataType;
                return new ArrayType<>(MapType.class, mapType);
            default:
                throw CommonError.unsupportedDataType("SeaTunnel", genericType, field);
        }
    }

    private static SeaTunnelDataType<?> parseDecimalType(String columnStr) {
        String[] decimalInfos = columnStr.split(",");
        if (decimalInfos.length < 2) {
            throw new RuntimeException(
                    "Decimal type should assign precision and scale information");
        }
        int precision = Integer.parseInt(decimalInfos[0].replaceAll("\\D", ""));
        int scale = Integer.parseInt(decimalInfos[1].replaceAll("\\D", ""));
        return new DecimalType(precision, scale);
    }
}
