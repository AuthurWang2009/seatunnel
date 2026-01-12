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

package org.apache.seatunnel.config;

import org.apache.seatunnel.config.utils.FileUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ConfigTest {

    @Test
    public void testConfigKeyOrder() throws URISyntaxException {
        String expected =
                "{\"env\":{\"job.mode\":\"BATCH\"},\"sink\":[{\"plugin_name\":\"Console\"}],\"source\":[{\"plugin_name\":\"FakeSource\",\"row.num\":100,\"schema\":{\"fields\":{\"age\":\"int\",\"name\":\"string\"}}}]}";

        Config config =
                ConfigFactory.parseFile(
                        FileUtils.getFileFromResources("/seatunnel/serialize.conf"));
        Assertions.assertEquals(expected, config.root().render(ConfigRenderOptions.concise()));
    }

    @Test
    public void testQuoteAsKey() throws URISyntaxException {
        Config config =
                ConfigFactory.parseFile(
                        FileUtils.getFileFromResources("/seatunnel/configWithSpecialKey.conf"));
        List<String> keys = new ArrayList<>(config.getObject("object").keySet());
        Assertions.assertTrue(keys.contains("\""));
        Assertions.assertTrue(keys.contains("\"\""));
        Assertions.assertTrue(keys.contains("\\\""));

        ConfigObject obj = config.getObject("object");
        Assertions.assertEquals("\\\"", obj.get("\"").unwrapped());
        Assertions.assertEquals("\\\"\\\"", obj.get("\"\"").unwrapped());
        Assertions.assertEquals("\\\\\\\"", obj.get("\\\"").unwrapped());
    }
}
