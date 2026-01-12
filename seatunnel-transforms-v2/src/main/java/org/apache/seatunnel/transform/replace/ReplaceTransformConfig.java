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

package org.apache.seatunnel.transform.replace;

import org.apache.seatunnel.api.config.Config;
import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ReplaceTransformConfig implements Serializable {

    public static final ConfigEntry<String> KEY_REPLACE_FIELD =
            ConfigOption.key("replace_field")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The field you want to replace");

    public static final ConfigEntry<String> KEY_PATTERN =
            ConfigOption.key("pattern")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The old string that will be replaced");

    public static final ConfigEntry<String> KEY_REPLACEMENT =
            ConfigOption.key("replacement")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The new string for replace");

    public static final ConfigEntry<Boolean> KEY_IS_REGEX =
            ConfigOption.key("is_regex")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Use regex for string match");

    public static final ConfigEntry<Boolean> KEY_REPLACE_FIRST =
            ConfigOption.key("replace_first")
                    .booleanType()
                    .noDefaultValue()
                    .withDescription("Replace the first match string");

    public static final ConfigEntry<List<TableTransforms>> MULTI_TABLES =
            ConfigOption.key("table_transform")
                    .listType(TableTransforms.class)
                    .noDefaultValue()
                    .withDescription("");

    private String replaceField;
    private String pattern;
    private String replacement;
    private Boolean isRegex;
    private Boolean replaceFirst;

    @Data
    public static class TableTransforms implements Serializable {
        @JsonAlias("table_path")
        private String tablePath;

        @JsonAlias("replace_field")
        private String replaceField;

        @JsonAlias("pattern")
        private String pattern;

        @JsonAlias("replacement")
        private String replacement;

        @JsonAlias("is_regex")
        private Boolean isRegex;

        @JsonAlias("replace_first")
        private Boolean replaceFirst;
    }

    public static ReplaceTransformConfig of(Config config) {
        ReplaceTransformConfig replaceTransformConfig = new ReplaceTransformConfig();
        replaceTransformConfig.setReplaceField(config.get(KEY_REPLACE_FIELD));
        replaceTransformConfig.setPattern(config.get(KEY_PATTERN));
        replaceTransformConfig.setReplacement(config.get(KEY_REPLACEMENT));
        replaceTransformConfig.setIsRegex(config.get(KEY_IS_REGEX));
        replaceTransformConfig.setReplaceFirst(config.get(KEY_REPLACE_FIRST));
        return replaceTransformConfig;
    }
}
