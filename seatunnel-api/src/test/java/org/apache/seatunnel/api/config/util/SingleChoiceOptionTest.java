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

package org.apache.seatunnel.api.config.util;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;
import org.apache.seatunnel.api.config.SingleChoiceConfigEntry;
import org.apache.seatunnel.api.sink.DataSaveMode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class SingleChoiceOptionTest {

    @Test
    public void test() {
        ConfigEntry<String> stringOption =
                ConfigOption.key("test_single_choice")
                        .singleChoice(String.class, Arrays.asList("A", "B", "C"))
                        .defaultValue("A");

        ConfigEntry<DataSaveMode> saveModeOption =
                ConfigOption.key("save_mode")
                        .singleChoice(
                                DataSaveMode.class,
                                Arrays.asList(DataSaveMode.APPEND_DATA, DataSaveMode.DROP_DATA))
                        .defaultValue(DataSaveMode.APPEND_DATA)
                        .withDescription("save mode test");

        OptionRule build = OptionRule.builder().optional(stringOption, saveModeOption).build();
        List<ConfigEntry<?>> optionalOptions = build.getOptionalOptions();
        ConfigEntry<?> option = optionalOptions.get(0);
        Assertions.assertTrue(SingleChoiceConfigEntry.class.isAssignableFrom(option.getClass()));
        SingleChoiceConfigEntry singleChoiceOption = (SingleChoiceConfigEntry) option;
        Assertions.assertEquals(3, singleChoiceOption.getOptionValues().size());
        Assertions.assertEquals("A", singleChoiceOption.defaultValue());

        option = optionalOptions.get(1);
        singleChoiceOption = (SingleChoiceConfigEntry) option;
        Assertions.assertEquals(2, singleChoiceOption.getOptionValues().size());
        Assertions.assertEquals(DataSaveMode.APPEND_DATA, singleChoiceOption.defaultValue());
    }
}
