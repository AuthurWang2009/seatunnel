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

package org.apache.seatunnel.core.starter.command;

import org.apache.seatunnel.api.config.Config;
import org.apache.seatunnel.api.config.ConfigLoader;
import org.apache.seatunnel.api.config.util.ConfigShadeUtils;
import org.apache.seatunnel.api.config.util.ConfigUtil;
import org.apache.seatunnel.core.starter.exception.CommandExecuteException;
import org.apache.seatunnel.core.starter.exception.ConfigCheckException;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.seatunnel.core.starter.utils.FileUtils.checkConfigExist;

@Slf4j
public class ConfDecryptCommand implements Command<AbstractCommandArgs> {

    private final AbstractCommandArgs abstractCommandArgs;

    public ConfDecryptCommand(AbstractCommandArgs abstractCommandArgs) {
        this.abstractCommandArgs = abstractCommandArgs;
    }

    @Override
    public void execute() throws CommandExecuteException, ConfigCheckException {
        String decryptConfigFile = abstractCommandArgs.getConfigFile();
        Path configPath = Paths.get(decryptConfigFile);
        checkConfigExist(configPath);
        Config config = ConfigLoader.load(configPath);
        Config decryptConfig = ConfigShadeUtils.decryptConfig(config);
        log.info("Decrypt config: \n{}", ConfigUtil.convertToJsonString(decryptConfig));
    }
}
