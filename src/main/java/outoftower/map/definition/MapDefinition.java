package outoftower.map.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapDefinition {
    private final int schemaVersion;
    private final String mapId;
    private final int mapVersion;
    private final int width;
    private final int height;
    private final int viewportRadius;
    private final List<NodeDefinition> nodes;
    private final Map<String, NodeDefinition> nodesById;
    private final Map<String, EventPoolDefinition> eventPools;
    private final Set<EdgeKey> baseEdges;

    public MapDefinition(int schemaVersion, String mapId, int mapVersion,
                         int width, int height, int viewportRadius,
                         List<NodeDefinition> nodes,
                         Map<String, EventPoolDefinition> eventPools,
                         Set<EdgeKey> baseEdges) {
        this.schemaVersion = schemaVersion;
        this.mapId = mapId;
        this.mapVersion = mapVersion;
        this.width = width;
        this.height = height;
        this.viewportRadius = viewportRadius;
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        LinkedHashMap<String, NodeDefinition> byId = new LinkedHashMap<>();
        for (NodeDefinition node : nodes) byId.put(node.getId(), node);
        this.nodesById = Collections.unmodifiableMap(byId);
        this.eventPools = Collections.unmodifiableMap(new LinkedHashMap<>(eventPools));
        this.baseEdges = Collections.unmodifiableSet(new LinkedHashSet<>(baseEdges));
    }

    public int getSchemaVersion() { return schemaVersion; }
    public String getMapId() { return mapId; }
    public int getMapVersion() { return mapVersion; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getViewportRadius() { return viewportRadius; }
    public List<NodeDefinition> getNodes() { return nodes; }
    public Map<String, NodeDefinition> getNodesById() { return nodesById; }
    public Map<String, EventPoolDefinition> getEventPools() { return eventPools; }
    public Set<EdgeKey> getBaseEdges() { return baseEdges; }
}
