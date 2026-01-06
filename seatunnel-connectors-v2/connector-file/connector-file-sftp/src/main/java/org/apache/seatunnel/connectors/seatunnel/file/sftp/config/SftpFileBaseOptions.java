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

package org.apache.seatunnel.connectors.seatunnel.file.sftp.config;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;
import org.apache.seatunnel.connectors.seatunnel.file.config.FileBaseOptions;

public class SftpFileBaseOptions extends FileBaseOptions {
    public static final ConfigEntry<String> SFTP_PASSWORD =
            ConfigOption.key("password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SFTP server password");
    public static final ConfigEntry<String> SFTP_USER =
            ConfigOption.key("user")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SFTP server username");
    public static final ConfigEntry<String> SFTP_HOST =
            ConfigOption.key("host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("SFTP server host");
    public static final ConfigEntry<Integer> SFTP_PORT =
            ConfigOption.key("port")
                    .intType()
                    .noDefaultValue()
                    .withDescription("SFTP server port");
}
