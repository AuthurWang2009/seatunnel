package org.apache.seatunnel.api.config;

import org.apache.seatunnel.common.config.ConfigType;
import org.apache.seatunnel.common.parser.ConfigParser;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class ConfigLoader {

    private ConfigLoader() {}

    public static Config load(Path path) {
        String fileName = path.getFileName().toString();
        ConfigType type = inferType(fileName);
        return load(path, type);
    }

    public static Config load(Path path, ConfigType type) {
        ConfigParser parser = findParser(type);
        return ReadonlyConfig.fromMap(parser.parse(path));
    }

    public static Config load(String content, ConfigType type) {
        ConfigParser parser = findParser(type);
        return ReadonlyConfig.fromMap(parser.parse(content));
    }

    /**
     * Load config from file with custom variable substitution.
     *
     * @param path the path to the configuration file
     * @param variables user-provided variables for substitution (can be null)
     * @return the loaded Config with variables resolved
     */
    public static Config load(Path path, Map<String, Object> variables) {
        String fileName = path.getFileName().toString();
        ConfigType type = inferType(fileName);
        return load(path, type, variables);
    }

    /**
     * Load config from file with custom variable substitution.
     *
     * @param path the path to the configuration file
     * @param type the config type
     * @param variables user-provided variables for substitution (can be null)
     * @return the loaded Config with variables resolved
     */
    public static Config load(Path path, ConfigType type, Map<String, Object> variables) {
        ConfigParser parser = findParser(type);
        return ReadonlyConfig.fromMap(parser.parse(path, variables));
    }

    /**
     * Load config from string content with custom variable substitution.
     *
     * @param content the configuration content
     * @param type the config type
     * @param variables user-provided variables for substitution (can be null)
     * @return the loaded Config with variables resolved
     */
    public static Config load(String content, ConfigType type, Map<String, Object> variables) {
        ConfigParser parser = findParser(type);
        return ReadonlyConfig.fromMap(parser.parse(content, variables));
    }

    private static ConfigType inferType(String fileName) {
        for (ConfigType type : ConfigType.values()) {
            for (String extension : type.getExtensions()) {
                if (fileName.endsWith(extension)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported file type: " + fileName);
    }

    private static ConfigParser findParser(ConfigType type) {
        ServiceLoader<ConfigParser> loader = ServiceLoader.load(ConfigParser.class);
        Iterator<ConfigParser> iterator = loader.iterator();
        while (iterator.hasNext()) {
            ConfigParser parser = iterator.next();
            if (parser.getConfigType() == type) {
                return parser;
            }
        }
        throw new UnsupportedOperationException("No parser found for type: " + type);
    }
}
