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

package org.apache.seatunnel.connectors.seatunnel.file.ftp.config;

import org.apache.seatunnel.api.config.ConfigEntry;
import org.apache.seatunnel.api.config.ConfigOption;
import org.apache.seatunnel.connectors.seatunnel.file.config.FileBaseOptions;
import org.apache.seatunnel.connectors.seatunnel.file.ftp.system.FtpConnectionMode;

import static org.apache.seatunnel.connectors.seatunnel.file.ftp.system.FtpConnectionMode.ACTIVE_LOCAL;

public class FtpFileBaseOptions extends FileBaseOptions {
    public static final ConfigEntry<String> FTP_PASSWORD =
            ConfigOption.key("password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("FTP server password");
    public static final ConfigEntry<String> FTP_USERNAME =
            ConfigOption.key("user")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("FTP server username");
    public static final ConfigEntry<String> FTP_HOST =
            ConfigOption.key("host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("FTP server host");
    public static final ConfigEntry<Integer> FTP_PORT =
            ConfigOption.key("port").intType().noDefaultValue().withDescription("FTP server port");
    public static final ConfigEntry<FtpConnectionMode> FTP_CONNECTION_MODE =
            ConfigOption.key("connection_mode")
                    .enumType(FtpConnectionMode.class)
                    .defaultValue(ACTIVE_LOCAL)
                    .withDescription("FTP server connection mode ");
    public static final ConfigEntry<Boolean> FTP_REMOTE_VERIFICATION_ENABLED =
            ConfigOption.key("remote_verification_enabled")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription(
                            "Whether to enable remote host verification for FTP data channels (enabled by default)");
}
