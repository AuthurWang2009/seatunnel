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

package org.apache.seatunnel.api.config;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestConfigUtils {

    /**
     * Loads a configuration file from the classpath and returns it as a ReadonlyConfig.
     *
     * @param resourcePath the classpath resource path (e.g., "/conf/option-test.conf")
     * @return the loaded ReadonlyConfig
     * @throws RuntimeException if the resource cannot be found or loaded
     */
    private static ReadonlyConfig loadConfig(String resourcePath) {
        URL resource = TestConfigUtils.class.getResource(resourcePath);
        if (resource == null) {
            throw new RuntimeException("Resource not found: " + resourcePath);
        }
        try {
            Path path = Paths.get(resource.toURI());
            return (ReadonlyConfig) ConfigLoader.load(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + resourcePath, e);
        }
    }

    public static ReadonlyConfig getOptionTestConfig() {
        return loadConfig("/conf/option-test.conf");
    }

    public static ReadonlyConfig getSimpleSchemaConfig() {
        return loadConfig("/conf/simple.schema.conf");
    }

    public static ReadonlyConfig getComplexSchemaConfig() {
        return loadConfig("/conf/complex.schema.conf");
    }

    public static ReadonlyConfig getSpecialSchemaConfig() {
        return loadConfig("/conf/config_special_schema.conf");
    }

    public static ReadonlyConfig getOneTableConfig() {
        return loadConfig("/conf/getCatalogTable.conf");
    }

    public static ReadonlyConfig getDefaultTablePathConfig() {
        return loadConfig("/conf/default_tablepath.conf");
    }

    public static ReadonlyConfig getGenericRowSchemaConfig() {
        return loadConfig("/conf/generic_row.schema.conf");
    }

    public static ReadonlyConfig getSchemaColumnConfig() {
        return loadConfig("/conf/catalog/schema_column.conf");
    }

    public static ReadonlyConfig getSchemaFieldConfig() {
        return loadConfig("/conf/catalog/schema_field.conf");
    }
}
