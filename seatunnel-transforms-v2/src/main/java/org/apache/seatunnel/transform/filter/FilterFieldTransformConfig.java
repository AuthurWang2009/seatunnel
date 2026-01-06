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

package org.apache.seatunnel.transform.filter;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class FilterFieldTransformConfig implements Serializable {

    public static final ConfigEntry<List<String>> INCLUDE_FIELDS =
            ConfigOption.key("include_fields")
                    .listType()
                    .noDefaultValue()
                    .withDescription("The list of fields that need to be kept.")
                    .withFallbackKeys("fields");

    public static final ConfigEntry<List<String>> EXCLUDE_FIELDS =
            ConfigOption.key("exclude_fields")
                    .listType()
                    .noDefaultValue()
                    .withDescription("The list of fields that need to be deleted");
}
