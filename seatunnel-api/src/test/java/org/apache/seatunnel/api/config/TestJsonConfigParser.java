package org.apache.seatunnel.api.config;

import org.apache.seatunnel.common.config.ConfigType;

public class TestJsonConfigParser extends TestHoconConfigParser {
    @Override
    public ConfigType getConfigType() {
        return ConfigType.JSON;
    }
}
