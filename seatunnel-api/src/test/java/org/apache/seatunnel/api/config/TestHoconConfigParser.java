package org.apache.seatunnel.api.config;

import org.apache.seatunnel.common.config.ConfigType;
import org.apache.seatunnel.common.parser.ConfigParser;

import com.typesafe.config.ConfigFactory;

import java.nio.file.Path;
import java.util.Map;

public class TestHoconConfigParser implements ConfigParser {
    @Override
    public ConfigType getConfigType() {
        return ConfigType.HOCON;
    }

    @Override
    public Map<String, Object> parse(Path path) {
        return ConfigFactory.parseFile(path.toFile()).root().unwrapped();
    }

    @Override
    public Map<String, Object> parse(String content) {
        return ConfigFactory.parseString(content).root().unwrapped();
    }
}
