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
import org.apache.seatunnel.api.config.OptionTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionRuleTest {
    public static final ConfigEntry<Long> TEST_TIMESTAMP =
            ConfigOption.key("option.timestamp")
                    .longType()
                    .noDefaultValue()
                    .withDescription("test long timestamp");

    public static final ConfigEntry<String> TEST_TOPIC_PATTERN =
            ConfigOption.key("option.topic-pattern")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("test string type");

    public static final ConfigEntry<List<String>> TEST_TOPIC =
            ConfigOption.key("option.topic")
                    .listType()
                    .noDefaultValue()
                    .withDescription("test list string type");

    public static final ConfigEntry<List<Integer>> TEST_PORTS =
            ConfigOption.key("option.ports")
                    .type(new TypeReference<List<Integer>>() {})
                    .noDefaultValue()
                    .withDescription("test list int type");

    public static final ConfigEntry<String> TEST_REQUIRED_HAVE_DEFAULT_VALUE =
            ConfigOption.key("option.required-have-default")
                    .stringType()
                    .defaultValue("11")
                    .withDescription("test string type");

    public static final ConfigEntry<String> TEST_DUPLICATE =
            ConfigOption.key("option.test-duplicate")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("test string type");

    @Test
    public void testBuildSuccess() {
        OptionRule rule =
                OptionRule.builder()
                        .optional(OptionTest.TEST_NUM, OptionTest.TEST_MODE)
                        .required(TEST_PORTS)
                        .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                        .conditional(
                                OptionTest.TEST_MODE, OptionTest.TestMode.TIMESTAMP, TEST_TIMESTAMP)
                        .build();
        Assertions.assertNotNull(rule);
    }

    @Test
    public void testVerify() {
        Executable executable =
                () -> {
                    OptionRule.builder()
                            .optional(OptionTest.TEST_NUM, OptionTest.TEST_MODE)
                            .required(TEST_PORTS, TEST_REQUIRED_HAVE_DEFAULT_VALUE)
                            .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.TIMESTAMP,
                                    TEST_TIMESTAMP)
                            .build();
                };

        executable =
                () -> {
                    OptionRule.builder()
                            .optional(
                                    OptionTest.TEST_NUM,
                                    OptionTest.TEST_MODE,
                                    TEST_REQUIRED_HAVE_DEFAULT_VALUE)
                            .required(TEST_PORTS, TEST_REQUIRED_HAVE_DEFAULT_VALUE)
                            .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.TIMESTAMP,
                                    TEST_TIMESTAMP)
                            .build();
                };

        // test duplicate
        assertEquals(
                "ErrorCode:[API-02], ErrorDescription:[Option item validate failed] - AbsolutelyRequiredOptions 'option.required-have-default' duplicate in option options.",
                assertThrows(OptionValidationException.class, executable).getMessage());

        executable =
                () -> {
                    OptionRule.builder()
                            .optional(OptionTest.TEST_NUM, OptionTest.TEST_MODE)
                            .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC, TEST_DUPLICATE)
                            .required(TEST_PORTS, TEST_DUPLICATE)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.TIMESTAMP,
                                    TEST_TIMESTAMP)
                            .build();
                };

        // test duplicate in RequiredOption$ExclusiveRequiredOptions
        assertEquals(
                "ErrorCode:[API-02], ErrorDescription:[Option item validate failed] - AbsolutelyRequiredOptions 'option.test-duplicate' duplicate in ExclusiveRequiredOptions options.",
                assertThrows(OptionValidationException.class, executable).getMessage());

        executable =
                () -> {
                    OptionRule.builder()
                            .optional(OptionTest.TEST_NUM)
                            .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                            .required(TEST_PORTS)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.TIMESTAMP,
                                    TEST_TIMESTAMP)
                            .build();
                };

        // test conditional not found in other options
        assertEquals(
                "ErrorCode:[API-02], ErrorDescription:[Option item validate failed] - Conditional 'option.mode' not found in options.",
                assertThrows(OptionValidationException.class, executable).getMessage());

        executable =
                () -> {
                    OptionRule.builder()
                            .optional(OptionTest.TEST_NUM, OptionTest.TEST_MODE)
                            .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                            .required(TEST_PORTS)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.TIMESTAMP,
                                    TEST_TIMESTAMP)
                            .conditional(OptionTest.TEST_NUM, 100, TEST_TIMESTAMP)
                            .build();
                };

        // test parameter can only be controlled by one other parameter
        assertEquals(
                "ErrorCode:[API-02], ErrorDescription:[Option item validate failed] - ConditionalRequiredOptions 'option.timestamp' duplicate in ConditionalRequiredOptions options.",
                assertThrows(OptionValidationException.class, executable).getMessage());

        // Test conditional only does not conflict with optional options
        // Test option TEST_TIMESTAMP
        executable =
                () -> {
                    OptionRule.builder()
                            .optional(OptionTest.TEST_NUM, OptionTest.TEST_MODE, TEST_TIMESTAMP)
                            .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                            .required(TEST_PORTS)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.TIMESTAMP,
                                    TEST_TIMESTAMP)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.LATEST,
                                    TEST_TIMESTAMP)
                            .build();
                };
        assertDoesNotThrow(executable);
        executable =
                () -> {
                    OptionRule.builder()
                            .optional(OptionTest.TEST_NUM, OptionTest.TEST_MODE)
                            .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC, TEST_TIMESTAMP)
                            .required(TEST_PORTS)
                            .conditional(
                                    OptionTest.TEST_MODE,
                                    OptionTest.TestMode.TIMESTAMP,
                                    TEST_TIMESTAMP)
                            .build();
                };
        assertEquals(
                "ErrorCode:[API-02], ErrorDescription:[Option item validate failed] - ConditionalRequiredOptions 'option.timestamp' duplicate in ExclusiveRequiredOptions options.",
                assertThrows(OptionValidationException.class, executable).getMessage());
    }

    @Test
    public void testEquals() {
        OptionRule rule1 =
                OptionRule.builder()
                        .optional(OptionTest.TEST_NUM, OptionTest.TEST_MODE)
                        .required(TEST_PORTS)
                        .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                        .conditional(
                                OptionTest.TEST_MODE, OptionTest.TestMode.TIMESTAMP, TEST_TIMESTAMP)
                        .build();
        OptionRule rule2 =
                OptionRule.builder()
                        .optional(OptionTest.TEST_NUM)
                        .optional(OptionTest.TEST_MODE)
                        .required(TEST_PORTS)
                        .exclusive(TEST_TOPIC_PATTERN, TEST_TOPIC)
                        .conditional(
                                OptionTest.TEST_MODE, OptionTest.TestMode.TIMESTAMP, TEST_TIMESTAMP)
                        .build();
        Assertions.assertEquals(rule1, rule2);
    }
}
