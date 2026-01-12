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

package org.apache.seatunnel.config.sql;

import org.apache.seatunnel.config.sql.model.Option;
import org.apache.seatunnel.config.sql.model.SeaTunnelConfig;
import org.apache.seatunnel.config.sql.model.SinkConfig;
import org.apache.seatunnel.config.sql.model.SourceConfig;
import org.apache.seatunnel.config.sql.model.TransformConfig;
import org.apache.seatunnel.config.sql.utils.Constant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigTemplate {
    private static void mergeGlobalConfig(List<String> envConfigs, Map<String, Object> rootMap) {
        // Env configs are raw strings like "key=value". We parse them individually.
        for (String envConfig : envConfigs) {
            try {
                com.typesafe.config.Config parsed =
                        com.typesafe.config.ConfigFactory.parseString(envConfig);
                parsed.entrySet()
                        .forEach(
                                entry -> rootMap.put(entry.getKey(), entry.getValue().unwrapped()));
            } catch (Exception e) {
                // ignore or log? usage is loose here.
            }
        }
    }

    private static List<Map<String, Object>> convertSourceConfig(List<SourceConfig> configs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SourceConfig config : configs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(Constant.PLUGIN_NAME_KEY, config.getConnector());
            for (Option option : config.getOptions()) {
                map.put(option.getKey(), option.getValue());
            }
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> convertSinkConfig(List<SinkConfig> configs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SinkConfig config : configs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(Constant.PLUGIN_NAME_KEY, config.getConnector());
            for (Option option : config.getOptions()) {
                map.put(option.getKey(), option.getValue());
            }
            list.add(map);
        }
        return list;
    }

    private static List<Map<String, Object>> convertTransformConfig(List<TransformConfig> configs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TransformConfig config : configs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(Constant.PLUGIN_NAME_KEY, Constant.SQL_TRANSFORM_PLUGIN_NAME);
            map.put(Constant.OPTION_PLUGIN_INPUT_KEY, config.getPluginInputIdentifier());
            map.put(Constant.OPTION_QUERY_KEY, config.getQuery());
            map.put(Constant.OPTION_PLUGIN_OUTPUT_KEY, config.getPluginOutputIdentifier());
            list.add(map);
        }
        return list;
    }

    public static org.apache.seatunnel.api.config.Config generate(SeaTunnelConfig seaTunnelConfig) {
        Map<String, Object> rootMap = new LinkedHashMap<>();
        mergeGlobalConfig(seaTunnelConfig.getEnvConfigs(), rootMap);

        rootMap.put(
                Constant.OPTION_TABLE_TYPE_SOURCE,
                convertSourceConfig(seaTunnelConfig.getSourceConfigs()));
        rootMap.put(
                Constant.OPTION_TABLE_TYPE_TRANSFORM,
                convertTransformConfig(seaTunnelConfig.getTransformConfigs()));
        rootMap.put(
                Constant.OPTION_TABLE_TYPE_SINK,
                convertSinkConfig(seaTunnelConfig.getSinkConfigs()));

        return org.apache.seatunnel.api.config.Config.of(rootMap);
    }
}
