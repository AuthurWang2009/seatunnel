package org.apache.seatunnel.api.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoaderTest {
    @Test
    public void testLoadHocon() throws URISyntaxException {
        Path path = getResourcePath("loader_test.conf");
        Config config = ConfigLoader.load(path);
        Assertions.assertEquals("value", config.getString("key"));
        Assertions.assertEquals("hocon value", config.getString("hocon.key"));
    }

    @Test
    public void testLoadJson() throws URISyntaxException {
        Path path = getResourcePath("loader_test.json");
        Config config = ConfigLoader.load(path);
        Assertions.assertEquals("value", config.getString("key"));
        Assertions.assertEquals("json value", config.getString("json.key"));
    }

    @Test
    public void testLoadYaml() throws URISyntaxException {
        Path path = getResourcePath("loader_test.yaml");
        Config config = ConfigLoader.load(path);
        Assertions.assertEquals("value", config.getString("key"));
        Assertions.assertEquals("yaml value", config.getString("yaml.key"));
    }

    @Test
    public void testLoadProperties() throws URISyntaxException {
        Path path = getResourcePath("loader_test.properties");
        Config config = ConfigLoader.load(path);
        Assertions.assertEquals("value", config.getString("key"));
        Assertions.assertEquals("properties value", config.getString("properties.key"));
    }

    private Path getResourcePath(String fileName) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
    }
}
