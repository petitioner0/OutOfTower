package outoftower.map.runtime;

import com.google.gson.JsonPrimitive;
import com.megacrit.cardcrawl.random.Random;
import org.junit.Test;
import outoftower.map.definition.MapDefinition;
import outoftower.map.definition.MapDefinitionLoader;
import outoftower.map.definition.MapDefinitionLoaderTest;
import outoftower.map.save.MapSaveDataV1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MapSessionTest {
    private static final Set<String> EVENTS = new HashSet<>(
            Arrays.asList("sample:first", "sample:second"));

    @Test
    public void sameMapRngStateProducesSameAssignments() {
        MapDefinition definition = definition();
        MapSession first = MapSession.createNew(1, definition, new Random(123456L));
        MapSession second = MapSession.createNew(1, definition, new Random(123456L));

        assertEquals(first.getCurrentNodeId(), second.getCurrentNodeId());
        assertEquals(first.getAssignedEvent("start"), second.getAssignedEvent("start"));
        assertEquals(first.getAssignedEvent("north"), second.getAssignedEvent("north"));
        assertNotEquals(first.getAssignedEvent("start"), first.getAssignedEvent("north"));
    }

    @Test
    public void runtimeEdgesAndStateRoundTrip() {
        MapDefinition definition = definition();
        MapSession original = MapSession.createNew(1, definition, new Random(9L));
        original.moveTo("start");
        assertTrue(original.disconnect("start", "north"));
        assertFalse(original.disconnect("start", "north"));
        original.state().put("sample:quest", "gateOpen", new JsonPrimitive(true));

        MapSaveDataV1 save = original.toSaveData();
        MapSession restored = MapSession.restoreForTests(1, definition, save, EVENTS);

        assertEquals(1, restored.getVisitCount("start"));
        assertFalse(restored.isConnected("start", "north"));
        assertTrue(restored.state().get("sample:quest", "gateOpen").getAsBoolean());
        assertEquals(original.getAssignedEvent("start"), restored.getAssignedEvent("start"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsRuntimeEdgeOutsideViewport() {
        MapSession session = MapSession.createNew(1, definitionWithFarNode(), new Random(1L));
        session.connect("start", "far");
    }

    private static MapDefinition definition() {
        return MapDefinitionLoader.parse(MapDefinitionLoaderTest.validJson(), EVENTS).getDefinition();
    }

    private static MapDefinition definitionWithFarNode() {
        String json = "{"
                + "\"schemaVersion\":1,\"mapId\":\"sample:far\",\"mapVersion\":1,"
                + "\"size\":{\"width\":5,\"height\":5},\"viewport\":{\"radius\":2},"
                + "\"eventPools\":{\"wild\":{\"drawMode\":\"WITH_REPLACEMENT\",\"events\":["
                + "{\"eventId\":\"sample:first\",\"weight\":1}]}},"
                + "\"nodes\":["
                + "{\"id\":\"start\",\"x\":0,\"y\":0,\"type\":\"wild\",\"start\":true,"
                + " \"eventPool\":\"wild\",\"links\":[]},"
                + "{\"id\":\"far\",\"x\":3,\"y\":3,\"type\":\"wild\",\"start\":false,"
                + " \"eventPool\":\"wild\",\"links\":[]}]}";
        return MapDefinitionLoader.parse(json, EVENTS).getDefinition();
    }
}
