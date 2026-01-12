package org.apache.seatunnel.common.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigVariableResolverTest {

    private Map<String, Object> config;

    @BeforeEach
    public void setUp() {
        config = new LinkedHashMap<>();
    }

    @Test
    public void testBasicVariableSubstitution() {
        config.put("host", "${DB_HOST}");
        config.put("port", "${DB_PORT}");

        Map<String, Object> variables = new HashMap<>();
        variables.put("DB_HOST", "localhost");
        variables.put("DB_PORT", "3306");

        Map<String, Object> result = ConfigVariableResolver.resolve(config, variables);

        Assertions.assertEquals("localhost", result.get("host"));
        Assertions.assertEquals("3306", result.get("port"));
    }

    @Test
    public void testDefaultValueSubstitution() {
        config.put("host", "${DB_HOST:localhost}");
        config.put("port", "${DB_PORT:3306}");

        // No variables provided, should use defaults
        Map<String, Object> result = ConfigVariableResolver.resolve(config, null);

        Assertions.assertEquals("localhost", result.get("host"));
        Assertions.assertEquals("3306", result.get("port"));
    }

    @Test
    public void testUserVariableOverridesDefault() {
        config.put("host", "${DB_HOST:localhost}");

        Map<String, Object> variables = Collections.singletonMap("DB_HOST", "prod-db.example.com");

        Map<String, Object> result = ConfigVariableResolver.resolve(config, variables);

        Assertions.assertEquals("prod-db.example.com", result.get("host"));
    }

    @Test
    public void testUserVariableOverridesSystemProperty() {
        String testKey = "test.variable.override." + System.currentTimeMillis();
        System.setProperty(testKey, "system-value");
        try {
            config.put("value", "${" + testKey + "}");

            Map<String, Object> variables = Collections.singletonMap(testKey, "user-value");

            Map<String, Object> result = ConfigVariableResolver.resolve(config, variables);

            // User variable should take priority over system property
            Assertions.assertEquals("user-value", result.get("value"));
        } finally {
            System.clearProperty(testKey);
        }
    }

    @Test
    public void testSystemPropertyFallback() {
        String testKey = "test.variable.sysprop." + System.currentTimeMillis();
        System.setProperty(testKey, "system-value");
        try {
            config.put("value", "${" + testKey + "}");

            // No user variable for this key
            Map<String, Object> variables = new HashMap<>();

            Map<String, Object> result = ConfigVariableResolver.resolve(config, variables);

            // Should fall back to system property
            Assertions.assertEquals("system-value", result.get("value"));
        } finally {
            System.clearProperty(testKey);
        }
    }

    @Test
    public void testUnresolvedVariableLeftAsIs() {
        config.put("value", "${UNDEFINED_VAR}");

        Map<String, Object> result = ConfigVariableResolver.resolve(config, new HashMap<>());

        // Unresolved variable should be left as-is
        Assertions.assertEquals("${UNDEFINED_VAR}", result.get("value"));
    }

    @Test
    public void testNestedMapResolution() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("host", "${DB_HOST}");
        nested.put("port", "${DB_PORT:5432}");
        config.put("database", nested);

        Map<String, Object> variables = Collections.singletonMap("DB_HOST", "db.example.com");

        Map<String, Object> result = ConfigVariableResolver.resolve(config, variables);

        @SuppressWarnings("unchecked")
        Map<String, Object> dbConfig = (Map<String, Object>) result.get("database");
        Assertions.assertEquals("db.example.com", dbConfig.get("host"));
        Assertions.assertEquals("5432", dbConfig.get("port"));
    }

    @Test
    public void testListResolution() {
        config.put("hosts", Arrays.asList("${HOST1}", "${HOST2:fallback}"));

        Map<String, Object> variables = Collections.singletonMap("HOST1", "host1.example.com");

        Map<String, Object> result = ConfigVariableResolver.resolve(config, variables);

        @SuppressWarnings("unchecked")
        List<String> hosts = (List<String>) result.get("hosts");
        Assertions.assertEquals("host1.example.com", hosts.get(0));
        Assertions.assertEquals("fallback", hosts.get(1));
    }

    @Test
    public void testMixedPlaceholderAndText() {
        config.put("url", "jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME}");

        Map<String, Object> variables = Collections.singletonMap("DB_NAME", "mydb");

        Map<String, Object> result = ConfigVariableResolver.resolve(config, variables);

        // Note: DB_NAME is substituted, DB_HOST and DB_PORT use defaults
        Assertions.assertEquals("jdbc:mysql://localhost:3306/mydb", result.get("url"));
    }

    @Test
    public void testNullConfig() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("key", "value");
        Map<String, Object> result = ConfigVariableResolver.resolve(null, variables);
        Assertions.assertNull(result);
    }

    @Test
    public void testEmptyConfig() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("key", "value");
        Map<String, Object> result = ConfigVariableResolver.resolve(new HashMap<>(), variables);
        Assertions.assertTrue(result.isEmpty());
    }
}
