package org.apache.seatunnel.api.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ConfigBuilderTest {
    @Test
    public void testPutAndBuild() {
        Config config =
                ConfigBuilder.create()
                        .put("key1", "value1")
                        .put("nested.key", "nestedValue")
                        .build();

        Assertions.assertEquals("value1", config.getString("key1"));
        Assertions.assertEquals("nestedValue", config.getString("nested.key"));
    }

    @Test
    public void testDeepMerge() {
        Config base = ConfigBuilder.create().put("a.b", "base").put("a.c", "base_c").build();

        Config overlay =
                ConfigBuilder.create().put("a.b", "overlay").put("a.d", "overlay_d").build();

        Config merged = ConfigBuilder.of(base).config(overlay).build();

        Assertions.assertEquals("overlay", merged.getString("a.b"));
        Assertions.assertEquals("base_c", merged.getString("a.c"));
        Assertions.assertEquals("overlay_d", merged.getString("a.d"));
    }

    @Test
    public void testOfMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        Config config = new ReadonlyConfig(map);

        Config build = ConfigBuilder.of(config).put("new", "val").build();
        Assertions.assertEquals("value", build.getString("key"));
        Assertions.assertEquals("val", build.getString("new"));
    }

    @Test
    public void testList() {
        Config config =
                ConfigBuilder.create()
                        .put("list", java.util.Arrays.asList("item1", "item2"))
                        .build();

        Assertions.assertEquals(2, config.getStringList("list").size());
        Assertions.assertEquals("item1", config.getStringList("list").get(0));
        Assertions.assertEquals("item2", config.getStringList("list").get(1));
    }

    @Test
    public void testComplexMapList() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("innerKey", "innerValue");
        nestedMap.put("innerList", java.util.Arrays.asList("a", "b"));

        Config config =
                ConfigBuilder.create()
                        .put("complex", nestedMap)
                        .put("topList", java.util.Collections.singletonList(nestedMap))
                        .build();

        Assertions.assertEquals("innerValue", config.getConfig("complex").getString("innerKey"));
        Assertions.assertEquals(2, config.getConfig("complex").getStringList("innerList").size());

        // Check list of maps
        // The current Config interface doesn't straightforwardly support getConfigList without
        // casting or using specialized methods if they existed.
        // But we can check internal structure via getValue or if standard Config supported it.
        // Assuming we rely on basic getValue for complex object retrieval validation or if we just
        // want to ensure it builds correctly.
        Assertions.assertNotNull(config.getValue("topList"));
        Assertions.assertTrue(config.getValue("topList") instanceof java.util.List);
    }
}
