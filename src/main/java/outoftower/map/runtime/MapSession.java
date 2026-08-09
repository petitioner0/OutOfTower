package outoftower.map.runtime;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.random.Random;
import outoftower.api.MapNodeView;
import outoftower.api.MapRuntime;
import outoftower.api.MapStateStore;
import outoftower.map.definition.DrawMode;
import outoftower.map.definition.EdgeKey;
import outoftower.map.definition.EventEntryDefinition;
import outoftower.map.definition.EventPoolDefinition;
import outoftower.map.definition.MapDefinition;
import outoftower.map.definition.MapDefinitionLoader;
import outoftower.map.definition.NodeDefinition;
import outoftower.map.nodes.room.OutOfTowerEventRoom;
import outoftower.map.save.EdgeSaveData;
import outoftower.map.save.MapSaveDataV1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mutable state for one map in one Act. */
public final class MapSession implements MapRuntime {
    private final int actIndex;
    private final MapDefinition definition;
    private final Map<String, String> assignedEvents;
    private final List<String> visitOrder = new ArrayList<>();
    private final Map<String, Integer> visitCounts = new LinkedHashMap<>();
    private final Set<EdgeKey> addedEdges = new LinkedHashSet<>();
    private final Set<EdgeKey> removedEdges = new LinkedHashSet<>();
    private final JsonObject extensionState;
    private final MapStateStore stateStore = new JsonMapStateStore();
    private final Map<String, MapRoomNode> nativeNodes = new LinkedHashMap<>();
    private final Map<String, OutOfTowerEventRoom> rooms = new LinkedHashMap<>();
    private String currentNodeId;
    private long visualRevision;

    private MapSession(
            int actIndex,
            MapDefinition definition,
            Map<String, String> assignedEvents,
            String currentNodeId,
            JsonObject extensionState) {
        this.actIndex = actIndex;
        this.definition = definition;
        this.assignedEvents = new LinkedHashMap<>(assignedEvents);
        this.currentNodeId = currentNodeId;
        this.extensionState = extensionState == null ? new JsonObject() : extensionState;
    }

    public static MapSession createNew(int actIndex, MapDefinition definition, Random mapRng) {
        if (mapRng == null) throw new IllegalStateException("AbstractDungeon.mapRng is not initialized");

        List<NodeDefinition> starts = new ArrayList<>();
        for (NodeDefinition node : definition.getNodes()) {
            if (node.isStart()) starts.add(node);
        }
        NodeDefinition start = starts.get(mapRng.random(starts.size() - 1));

        Map<String, List<EventEntryDefinition>> remainingByPool = new LinkedHashMap<>();
        for (EventPoolDefinition pool : definition.getEventPools().values()) {
            remainingByPool.put(pool.getId(), new ArrayList<>(pool.getEvents()));
        }

        LinkedHashMap<String, String> assignments = new LinkedHashMap<>();
        // List order is the JSON declaration order and therefore also the RNG call order.
        for (NodeDefinition node : definition.getNodes()) {
            EventPoolDefinition pool = definition.getEventPools().get(node.getEventPool());
            List<EventEntryDefinition> candidates = pool.getDrawMode() == DrawMode.WITHOUT_REPLACEMENT
                    ? remainingByPool.get(pool.getId()) : pool.getEvents();
            int selectedIndex = selectWeightedIndex(candidates, mapRng);
            assignments.put(node.getId(), candidates.get(selectedIndex).getEventId());
            if (pool.getDrawMode() == DrawMode.WITHOUT_REPLACEMENT) candidates.remove(selectedIndex);
        }
        return new MapSession(actIndex, definition, assignments, start.getId(), new JsonObject());
    }

    public static MapSession restore(int actIndex, MapDefinition definition, MapSaveDataV1 saveData) {
        return restoreInternal(actIndex, definition, saveData, new EventIdLookup() {
            @Override
            public boolean contains(String eventId) {
                return ContentRegistry.hasEvent(eventId);
            }
        });
    }

    static MapSession restoreForTests(
            int actIndex,
            MapDefinition definition,
            MapSaveDataV1 saveData,
            final Set<String> registeredEventIds) {
        return restoreInternal(actIndex, definition, saveData, new EventIdLookup() {
            @Override
            public boolean contains(String eventId) {
                return registeredEventIds.contains(eventId);
            }
        });
    }

    private static MapSession restoreInternal(
            int actIndex,
            MapDefinition definition,
            MapSaveDataV1 saveData,
            EventIdLookup eventIds) {
        List<String> errors = new ArrayList<>();
        if (saveData.schemaVersion != 1) errors.add("Unsupported save schemaVersion " + saveData.schemaVersion);
        if (saveData.actIndex != actIndex) {
            errors.add("Save belongs to Act " + saveData.actIndex + " but current Act is " + actIndex);
        }
        if (!definition.getMapId().equals(saveData.mapId)) {
            errors.add("Save expects map " + saveData.mapId + " but content provides " + definition.getMapId());
        }
        if (!definition.getNodesById().containsKey(saveData.currentNodeId)) {
            errors.add("Saved current node no longer exists: " + saveData.currentNodeId);
        }

        LinkedHashMap<String, String> assignments = new LinkedHashMap<>();
        for (NodeDefinition node : definition.getNodes()) {
            String eventId = saveData.assignedEvents == null ? null : saveData.assignedEvents.get(node.getId());
            if (eventId == null) {
                errors.add("Save has no event assignment for node " + node.getId());
            } else if (!eventIds.contains(eventId)) {
                errors.add("Saved event is no longer registered: " + eventId);
            } else {
                assignments.put(node.getId(), eventId);
            }
        }

        if (!errors.isEmpty()) throw new IllegalStateException(joinErrors("Cannot restore OutOfTower map", errors));
        MapSession session = new MapSession(
                actIndex,
                definition,
                assignments,
                saveData.currentNodeId,
                saveData.extensionState == null ? new JsonObject() : saveData.extensionState);

        if (saveData.visitOrder != null) {
            for (String nodeId : saveData.visitOrder) {
                session.requireNode(nodeId);
                if (!session.visitOrder.contains(nodeId)) session.visitOrder.add(nodeId);
            }
        }
        if (saveData.visitCounts != null) {
            for (Map.Entry<String, Integer> entry : saveData.visitCounts.entrySet()) {
                session.requireNode(entry.getKey());
                if (entry.getValue() == null || entry.getValue() < 0) {
                    throw new IllegalStateException("Invalid saved visit count for " + entry.getKey());
                }
                session.visitCounts.put(entry.getKey(), entry.getValue());
            }
        }
        session.restoreEdges(saveData.addedEdges, true);
        session.restoreEdges(saveData.removedEdges, false);
        return session;
    }

    public MapSaveDataV1 toSaveData() {
        MapSaveDataV1 data = new MapSaveDataV1();
        data.actIndex = actIndex;
        data.mapId = definition.getMapId();
        data.mapVersion = definition.getMapVersion();
        data.currentNodeId = currentNodeId;
        data.visitOrder.addAll(visitOrder);
        data.visitCounts.putAll(visitCounts);
        data.assignedEvents.putAll(assignedEvents);
        for (EdgeKey edge : addedEdges) {
            data.addedEdges.add(new EdgeSaveData(edge.getFirst(), edge.getSecond()));
        }
        for (EdgeKey edge : removedEdges) {
            data.removedEdges.add(new EdgeSaveData(edge.getFirst(), edge.getSecond()));
        }
        data.extensionState = extensionState;
        return data;
    }

    @Override
    public String getMapId() { return definition.getMapId(); }

    @Override
    public int getActIndex() { return actIndex; }

    @Override
    public String getCurrentNodeId() { return currentNodeId; }

    @Override
    public MapNodeView getNode(String nodeId) {
        NodeDefinition node = definition.getNodesById().get(nodeId);
        return node == null ? null : toView(node);
    }

    @Override
    public Collection<MapNodeView> getNodes() {
        List<MapNodeView> views = new ArrayList<>();
        for (NodeDefinition node : definition.getNodes()) views.add(toView(node));
        return Collections.unmodifiableList(views);
    }

    @Override
    public int getVisitCount(String nodeId) {
        requireNode(nodeId);
        return visitCounts.getOrDefault(nodeId, 0);
    }

    @Override
    public boolean hasVisited(String nodeId) {
        requireNode(nodeId);
        return visitCounts.getOrDefault(nodeId, 0) > 0;
    }

    @Override
    public boolean isConnected(String firstNodeId, String secondNodeId) {
        requireNode(firstNodeId);
        requireNode(secondNodeId);
        EdgeKey edge = EdgeKey.of(firstNodeId, secondNodeId);
        return addedEdges.contains(edge)
                || definition.getBaseEdges().contains(edge) && !removedEdges.contains(edge);
    }

    @Override
    public boolean connect(String firstNodeId, String secondNodeId) {
        validateMutableEdge(firstNodeId, secondNodeId);
        EdgeKey edge = EdgeKey.of(firstNodeId, secondNodeId);
        if (isConnected(firstNodeId, secondNodeId)) return false;
        if (definition.getBaseEdges().contains(edge)) removedEdges.remove(edge);
        else addedEdges.add(edge);
        visualRevision++;
        return true;
    }

    @Override
    public boolean disconnect(String firstNodeId, String secondNodeId) {
        validateMutableEdge(firstNodeId, secondNodeId);
        EdgeKey edge = EdgeKey.of(firstNodeId, secondNodeId);
        if (!isConnected(firstNodeId, secondNodeId)) return false;
        if (definition.getBaseEdges().contains(edge)) removedEdges.add(edge);
        else addedEdges.remove(edge);
        visualRevision++;
        return true;
    }

    @Override
    public MapStateStore state() { return stateStore; }

    public boolean canReach(String targetNodeId) {
        requireNode(targetNodeId);
        if (currentNodeId == null) return false;
        if (!hasVisited(currentNodeId)) return currentNodeId.equals(targetNodeId);
        return isConnected(currentNodeId, targetNodeId);
    }

    public void moveTo(String targetNodeId) {
        if (!canReach(targetNodeId)) {
            throw new IllegalStateException("Node " + targetNodeId + " is not reachable from " + currentNodeId);
        }
        currentNodeId = targetNodeId;
        if (!visitOrder.contains(targetNodeId)) visitOrder.add(targetNodeId);
        visitCounts.put(targetNodeId, visitCounts.getOrDefault(targetNodeId, 0) + 1);
        visualRevision++;
    }

    public Set<EdgeKey> getEffectiveEdges() {
        LinkedHashSet<EdgeKey> result = new LinkedHashSet<>(definition.getBaseEdges());
        result.removeAll(removedEdges);
        result.addAll(addedEdges);
        return result;
    }

    public MapDefinition getDefinition() { return definition; }
    public String getAssignedEvent(String nodeId) { return assignedEvents.get(nodeId); }
    public Map<String, MapRoomNode> getNativeNodes() { return nativeNodes; }
    public Map<String, OutOfTowerEventRoom> getRooms() { return rooms; }
    public long getVisualRevision() { return visualRevision; }

    public void clearPresentation() {
        nativeNodes.clear();
        rooms.clear();
        visualRevision++;
    }

    private MapNodeView toView(NodeDefinition node) {
        return new MapNodeView(node.getId(), node.getX(), node.getY(), node.getType(),
                node.getEventPool(), assignedEvents.get(node.getId()));
    }

    private void validateMutableEdge(String firstNodeId, String secondNodeId) {
        NodeDefinition first = requireNode(firstNodeId);
        NodeDefinition second = requireNode(secondNodeId);
        if (firstNodeId.equals(secondNodeId)) throw new IllegalArgumentException("A node cannot connect to itself");
        int distance = Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY());
        if (distance > definition.getViewportRadius()) {
            throw new IllegalArgumentException("Edge distance " + distance
                    + " exceeds viewport radius " + definition.getViewportRadius());
        }
    }

    private NodeDefinition requireNode(String nodeId) {
        NodeDefinition node = definition.getNodesById().get(nodeId);
        if (node == null) throw new IllegalArgumentException("Unknown node id: " + nodeId);
        return node;
    }

    private void restoreEdges(List<EdgeSaveData> savedEdges, boolean added) {
        if (savedEdges == null) return;
        for (EdgeSaveData saved : savedEdges) {
            if (saved == null || saved.first == null || saved.second == null) {
                throw new IllegalStateException("Saved edge is incomplete");
            }
            validateMutableEdge(saved.first, saved.second);
            EdgeKey edge = EdgeKey.of(saved.first, saved.second);
            if (added) {
                if (definition.getBaseEdges().contains(edge)) {
                    throw new IllegalStateException("Saved added edge is already a base edge: " + edge);
                }
                addedEdges.add(edge);
            } else {
                if (!definition.getBaseEdges().contains(edge)) {
                    throw new IllegalStateException("Saved removed edge is not a base edge: " + edge);
                }
                removedEdges.add(edge);
            }
        }
    }

    private static int selectWeightedIndex(List<EventEntryDefinition> entries, Random rng) {
        int totalWeight = 0;
        for (EventEntryDefinition entry : entries) {
            if (Integer.MAX_VALUE - totalWeight < entry.getWeight()) {
                throw new IllegalStateException("Event pool total weight exceeds integer range");
            }
            totalWeight += entry.getWeight();
        }
        int roll = rng.random(totalWeight - 1);
        for (int index = 0; index < entries.size(); index++) {
            roll -= entries.get(index).getWeight();
            if (roll < 0) return index;
        }
        throw new IllegalStateException("Could not select a weighted event");
    }

    private static String joinErrors(String heading, List<String> errors) {
        StringBuilder result = new StringBuilder(heading);
        for (String error : errors) result.append("\n - ").append(error);
        return result.toString();
    }

    private interface EventIdLookup {
        boolean contains(String eventId);
    }

    private final class JsonMapStateStore implements MapStateStore {
        @Override
        public JsonElement get(String namespace, String key) {
            JsonObject values = namespace(namespace, false);
            return values == null ? null : values.get(requireKey(key));
        }

        @Override
        public void put(String namespace, String key, JsonElement value) {
            if (value == null) throw new IllegalArgumentException("State value must not be null");
            namespace(namespace, true).add(requireKey(key), value);
        }

        @Override
        public JsonElement remove(String namespace, String key) {
            JsonObject values = namespace(namespace, false);
            return values == null ? null : values.remove(requireKey(key));
        }

        @Override
        public boolean contains(String namespace, String key) {
            JsonObject values = namespace(namespace, false);
            return values != null && values.has(requireKey(key));
        }

        private JsonObject namespace(String namespace, boolean create) {
            if (!MapDefinitionLoader.isNamespacedId(namespace)) {
                throw new IllegalArgumentException("State namespace must use the form modid:name");
            }
            JsonElement existing = extensionState.get(namespace);
            if (existing == null) {
                if (!create) return null;
                JsonObject values = new JsonObject();
                extensionState.add(namespace, values);
                return values;
            }
            if (!existing.isJsonObject()) {
                throw new IllegalStateException("State namespace is not a JSON object: " + namespace);
            }
            return existing.getAsJsonObject();
        }

        private String requireKey(String key) {
            if (key == null || key.trim().isEmpty()) throw new IllegalArgumentException("State key must not be blank");
            return key.trim();
        }
    }
}
