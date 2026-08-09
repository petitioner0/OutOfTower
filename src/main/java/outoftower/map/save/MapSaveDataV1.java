package outoftower.map.save;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MapSaveDataV1 {
    public int schemaVersion = 1;
    public int actIndex;
    public String mapId;
    public int mapVersion;
    public String currentNodeId;
    public List<String> visitOrder = new ArrayList<>();
    public Map<String, Integer> visitCounts = new LinkedHashMap<>();
    public Map<String, String> assignedEvents = new LinkedHashMap<>();
    public List<EdgeSaveData> addedEdges = new ArrayList<>();
    public List<EdgeSaveData> removedEdges = new ArrayList<>();
    public JsonObject extensionState = new JsonObject();
}
