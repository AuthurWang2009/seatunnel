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

package org.apache.seatunnel.config.sql.spi;

import org.apache.seatunnel.config.sql.model.BaseConfig;
import org.apache.seatunnel.config.sql.model.SeaTunnelConfig;

import net.sf.jsqlparser.statement.Statement;

import java.util.Map;

public interface SqlParserSpi {
    /**
     * Parse the standard JSqlParser Statement into SeaTunnel Config.
     *
     * @param statement JSqlParser Statement
     * @param sqlTables context map of table names to their configs
     * @param seaTunnelConfig the main config object to populate
     * @return true if the statement was handled, false otherwise
     */
    boolean parse(
            Statement statement,
            Map<String, BaseConfig> sqlTables,
            SeaTunnelConfig seaTunnelConfig);
}
