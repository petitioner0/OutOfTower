package outoftower.map.definition;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MapDefinitionLoaderTest {
    @Test
    public void parsesValidMapInJsonOrder() {
        MapDefinition definition = MapDefinitionLoader.parse(validJson(),
                Arrays.asList("sample:first", "sample:second")).getDefinition();

        assertEquals("sample:act1", definition.getMapId());
        assertEquals(2, definition.getNodes().size());
        assertEquals("start", definition.getNodes().get(0).getId());
        assertEquals("north", definition.getNodes().get(1).getId());
        assertEquals(1, definition.getBaseEdges().size());
    }

    @Test
    public void aggregatesIndependentValidationErrors() {
        String json = "{"
                + "\"schemaVersion\":2,"
                + "\"mapId\":\"bad\","
                + "\"mapVersion\":0,"
                + "\"size\":{\"width\":0,\"height\":0},"
                + "\"viewport\":{\"radius\":8},"
                + "\"eventPools\":{},"
                + "\"nodes\":[]"
                + "}";
        try {
            MapDefinitionLoader.parse(json, Arrays.<String>asList());
            fail("Expected validation failure");
        } catch (MapValidationException exception) {
            assertTrue(exception.getErrors().size() >= 7);
            assertTrue(exception.getMessage().contains("schemaVersion"));
            assertTrue(exception.getMessage().contains("mapId"));
        }
    }

    @Test
    public void rejectsEdgesOutsideViewport() {
        String json = validJson().replace("\"x\":1,\"y\":0", "\"x\":3,\"y\":0");
        try {
            MapDefinitionLoader.parse(json, Arrays.asList("sample:first", "sample:second"));
            fail("Expected long edge failure");
        } catch (MapValidationException exception) {
            assertTrue(exception.getMessage().contains("greater than viewport.radius"));
        }
    }

    @Test
    public void sampleContentMapMatchesThePublicSchema() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(
                "examples/sample-content/src/main/resources/samplemodResources/maps/act1.json"));
        MapDefinition definition = MapDefinitionLoader.parse(
                new String(bytes, StandardCharsets.UTF_8),
                Arrays.asList("samplemod:first", "samplemod:second")).getDefinition();

        assertEquals("samplemod:act1", definition.getMapId());
        assertEquals(3, definition.getNodes().size());
    }

    public static String validJson() {
        return "{"
                + "\"schemaVersion\":1,"
                + "\"mapId\":\"sample:act1\","
                + "\"mapVersion\":1,"
                + "\"size\":{\"width\":4,\"height\":4},"
                + "\"viewport\":{\"radius\":2},"
                + "\"eventPools\":{"
                + "  \"wild\":{"
                + "    \"drawMode\":\"WITHOUT_REPLACEMENT\","
                + "    \"events\":["
                + "      {\"eventId\":\"sample:first\",\"weight\":1},"
                + "      {\"eventId\":\"sample:second\",\"weight\":3}"
                + "    ]"
                + "  }"
                + "},"
                + "\"nodes\":["
                + "  {\"id\":\"start\",\"x\":0,\"y\":0,\"type\":\"wild\","
                + "   \"start\":true,\"eventPool\":\"wild\",\"links\":[\"north\"]},"
                + "  {\"id\":\"north\",\"x\":1,\"y\":0,\"type\":\"wild\","
                + "   \"start\":false,\"eventPool\":\"wild\",\"links\":[]}"
                + "]"
                + "}";
    }
}
