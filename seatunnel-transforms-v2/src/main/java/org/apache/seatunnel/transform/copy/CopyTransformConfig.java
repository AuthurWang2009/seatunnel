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

package org.apache.seatunnel.transform.copy;

import org.apache.seatunnel.api.config.Config;
import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class CopyTransformConfig implements Serializable {
    @Deprecated
    public static final ConfigEntry<String> SRC_FIELD =
            ConfigOption.key("src_field")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Src field you want to copy");

    @Deprecated
    public static final ConfigEntry<String> DEST_FIELD =
            ConfigOption.key("dest_field")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Copy Src field to Dest field");

    public static final ConfigEntry<Map<String, String>> FIELDS =
            ConfigOption.key("fields")
                    .mapType()
                    .noDefaultValue()
                    .withDescription(
                            "Specify the field copy relationship between input and output");

    private LinkedHashMap<String, String> fields;

    public static CopyTransformConfig of(Config config) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        Optional<Map<String, String>> optional = config.getOptional(FIELDS);
        if (optional.isPresent()) {
            fields.putAll(config.get(FIELDS));
        } else {
            fields.put(config.get(DEST_FIELD), config.get(SRC_FIELD));
        }

        CopyTransformConfig copyTransformConfig = new CopyTransformConfig();
        copyTransformConfig.setFields(fields);
        return copyTransformConfig;
    }
}
