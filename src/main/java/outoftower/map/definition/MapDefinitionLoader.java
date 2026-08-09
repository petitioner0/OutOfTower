package outoftower.map.definition;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Parses the public JSON schema and compiles it into an immutable definition. */
public final class MapDefinitionLoader {
    public static final int SCHEMA_VERSION = 1;
    private static final Pattern NAMESPACED_ID = Pattern.compile("[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+");

    private MapDefinitionLoader() {
    }

    public static MapLoadResult parse(String json, Collection<String> registeredEventIds) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        RawMap raw;
        try {
            raw = new Gson().fromJson(json, RawMap.class);
        } catch (JsonParseException exception) {
            errors.add("JSON syntax error: " + exception.getMessage());
            throw new MapValidationException(errors);
        }

        if (raw == null) {
            errors.add("The map resource is empty");
            throw new MapValidationException(errors);
        }

        if (raw.schemaVersion != SCHEMA_VERSION) {
            errors.add("schemaVersion must be " + SCHEMA_VERSION + " but was " + raw.schemaVersion);
        }
        validateNamespacedId("mapId", raw.mapId, errors);
        if (raw.mapVersion < 1) errors.add("mapVersion must be at least 1");
        if (raw.size == null) {
            errors.add("size is required");
        } else {
            if (raw.size.width < 1) errors.add("size.width must be at least 1");
            if (raw.size.height < 1) errors.add("size.height must be at least 1");
        }
        if (raw.viewport == null) {
            errors.add("viewport is required");
        } else if (raw.viewport.radius < 1 || raw.viewport.radius > 3) {
            errors.add("viewport.radius must be between 1 and 3");
        }

        LinkedHashMap<String, EventPoolDefinition> pools = compilePools(
                raw.eventPools, registeredEventIds, errors);
        List<NodeDefinition> nodes = compileNodes(raw, errors);

        LinkedHashMap<String, NodeDefinition> nodesById = new LinkedHashMap<>();
        Set<String> occupiedCoordinates = new HashSet<>();
        int startCount = 0;
        for (NodeDefinition node : nodes) {
            if (nodesById.put(node.getId(), node) != null) {
                errors.add("Duplicate node id: " + node.getId());
            }
            String coordinate = node.getX() + "," + node.getY();
            if (!occupiedCoordinates.add(coordinate)) {
                errors.add("Multiple nodes occupy coordinate " + coordinate);
            }
            if (raw.size != null && (node.getX() < 0 || node.getX() >= raw.size.width
                    || node.getY() < 0 || node.getY() >= raw.size.height)) {
                errors.add("Node " + node.getId() + " is outside the declared map size");
            }
            if (!pools.containsKey(node.getEventPool())) {
                errors.add("Node " + node.getId() + " references unknown event pool " + node.getEventPool());
            }
            if (node.isStart()) startCount++;
        }
        if (startCount == 0) errors.add("At least one node must have start=true");

        LinkedHashSet<EdgeKey> edges = new LinkedHashSet<>();
        Set<String> declaredDirections = new HashSet<>();
        int radius = raw.viewport == null ? 0 : raw.viewport.radius;
        for (NodeDefinition node : nodes) {
            for (String targetId : node.getLinks()) {
                String directionKey = node.getId() + "->" + targetId;
                if (!declaredDirections.add(directionKey)) {
                    errors.add("Node " + node.getId() + " declares link " + targetId + " more than once");
                    continue;
                }
                NodeDefinition target = nodesById.get(targetId);
                if (target == null) {
                    errors.add("Node " + node.getId() + " links to unknown node " + targetId);
                    continue;
                }
                if (node.getId().equals(targetId)) {
                    errors.add("Node " + node.getId() + " cannot link to itself");
                    continue;
                }
                EdgeKey edge = EdgeKey.of(node.getId(), targetId);
                if (!edges.add(edge)) {
                    errors.add("Undirected edge " + edge + " is declared more than once");
                    continue;
                }
                int distance = Math.abs(node.getX() - target.getX()) + Math.abs(node.getY() - target.getY());
                if (radius > 0 && distance > radius) {
                    errors.add("Edge " + edge + " has distance " + distance
                            + ", greater than viewport.radius " + radius);
                }
            }
        }

        validateWithoutReplacementCapacity(nodes, pools, errors);
        warnAboutDisconnectedNodes(nodes, edges, warnings);

        if (!errors.isEmpty()) throw new MapValidationException(errors);
        return new MapLoadResult(new MapDefinition(
                raw.schemaVersion,
                raw.mapId.trim(),
                raw.mapVersion,
                raw.size.width,
                raw.size.height,
                raw.viewport.radius,
                nodes,
                pools,
                edges
        ), warnings);
    }

    public static boolean isNamespacedId(String value) {
        return value != null && NAMESPACED_ID.matcher(value).matches();
    }

    private static LinkedHashMap<String, EventPoolDefinition> compilePools(
            Map<String, RawEventPool> rawPools,
            Collection<String> registeredEventIds,
            List<String> errors) {
        LinkedHashMap<String, EventPoolDefinition> pools = new LinkedHashMap<>();
        if (rawPools == null || rawPools.isEmpty()) {
            errors.add("eventPools must contain at least one pool");
            return pools;
        }
        Set<String> knownEvents = new HashSet<>(registeredEventIds == null
                ? Collections.<String>emptySet() : registeredEventIds);
        for (Map.Entry<String, RawEventPool> poolEntry : rawPools.entrySet()) {
            String poolId = poolEntry.getKey();
            RawEventPool rawPool = poolEntry.getValue();
            if (isBlank(poolId)) {
                errors.add("Event pool ids must not be blank");
                continue;
            }
            if (rawPool == null) {
                errors.add("Event pool " + poolId + " must be an object");
                continue;
            }
            DrawMode drawMode = null;
            try {
                drawMode = DrawMode.valueOf(rawPool.drawMode == null ? "" : rawPool.drawMode.trim());
            } catch (IllegalArgumentException ignored) {
                errors.add("Event pool " + poolId
                        + " drawMode must be WITH_REPLACEMENT or WITHOUT_REPLACEMENT");
            }
            List<EventEntryDefinition> events = new ArrayList<>();
            Set<String> seenEvents = new HashSet<>();
            long totalWeight = 0L;
            if (rawPool.events == null || rawPool.events.isEmpty()) {
                errors.add("Event pool " + poolId + " must contain at least one event");
            } else {
                for (int index = 0; index < rawPool.events.size(); index++) {
                    RawEventEntry rawEvent = rawPool.events.get(index);
                    if (rawEvent == null) {
                        errors.add("Event pool " + poolId + " contains a null entry at index " + index);
                        continue;
                    }
                    validateNamespacedId("eventId in pool " + poolId, rawEvent.eventId, errors);
                    if (rawEvent.weight < 1) {
                        errors.add("Event " + rawEvent.eventId + " in pool " + poolId
                                + " must have a positive integer weight");
                    }
                    if (!isBlank(rawEvent.eventId) && !seenEvents.add(rawEvent.eventId)) {
                        errors.add("Event pool " + poolId + " contains duplicate event " + rawEvent.eventId);
                    }
                    if (!isBlank(rawEvent.eventId) && !knownEvents.contains(rawEvent.eventId)) {
                        errors.add("Event pool " + poolId + " references unregistered event " + rawEvent.eventId);
                    }
                    if (!isBlank(rawEvent.eventId) && rawEvent.weight > 0) {
                        events.add(new EventEntryDefinition(rawEvent.eventId, rawEvent.weight));
                        totalWeight += rawEvent.weight;
                    }
                }
            }
            if (totalWeight > Integer.MAX_VALUE) {
                errors.add("Event pool " + poolId + " total weight exceeds " + Integer.MAX_VALUE);
            }
            if (drawMode != null) pools.put(poolId, new EventPoolDefinition(poolId, drawMode, events));
        }
        return pools;
    }

    private static List<NodeDefinition> compileNodes(RawMap raw, List<String> errors) {
        List<NodeDefinition> nodes = new ArrayList<>();
        if (raw.nodes == null || raw.nodes.isEmpty()) {
            errors.add("nodes must contain at least one node");
            return nodes;
        }
        for (int index = 0; index < raw.nodes.size(); index++) {
            RawNode rawNode = raw.nodes.get(index);
            if (rawNode == null) {
                errors.add("nodes contains a null entry at index " + index);
                continue;
            }
            if (isBlank(rawNode.id)) errors.add("Node at index " + index + " has a blank id");
            if (isBlank(rawNode.type)) errors.add("Node " + rawNode.id + " has a blank type");
            if (isBlank(rawNode.eventPool)) errors.add("Node " + rawNode.id + " has a blank eventPool");
            IconDefinition icon = null;
            if (rawNode.icon != null) {
                if (isBlank(rawNode.icon.image) || isBlank(rawNode.icon.outline)) {
                    errors.add("Node " + rawNode.id + " icon must provide both image and outline");
                } else {
                    icon = new IconDefinition(rawNode.icon.image.trim(), rawNode.icon.outline.trim());
                }
            }
            List<String> links = new ArrayList<>();
            if (rawNode.links != null) {
                for (String link : rawNode.links) {
                    if (isBlank(link)) {
                        errors.add("Node " + rawNode.id + " contains a blank link target");
                    } else {
                        links.add(link.trim());
                    }
                }
            }
            nodes.add(new NodeDefinition(
                    rawNode.id == null ? "" : rawNode.id.trim(),
                    rawNode.x,
                    rawNode.y,
                    rawNode.type == null ? "" : rawNode.type.trim(),
                    rawNode.start,
                    rawNode.eventPool == null ? "" : rawNode.eventPool.trim(),
                    links,
                    icon
            ));
        }
        return nodes;
    }

    private static void validateWithoutReplacementCapacity(
            List<NodeDefinition> nodes,
            Map<String, EventPoolDefinition> pools,
            List<String> errors) {
        Map<String, Integer> uses = new HashMap<>();
        for (NodeDefinition node : nodes) {
            uses.put(node.getEventPool(), uses.getOrDefault(node.getEventPool(), 0) + 1);
        }
        for (EventPoolDefinition pool : pools.values()) {
            if (pool.getDrawMode() == DrawMode.WITHOUT_REPLACEMENT
                    && uses.getOrDefault(pool.getId(), 0) > pool.getEvents().size()) {
                errors.add("Event pool " + pool.getId() + " is used by "
                        + uses.get(pool.getId()) + " nodes but contains only "
                        + pool.getEvents().size() + " events for WITHOUT_REPLACEMENT");
            }
        }
    }

    private static void warnAboutDisconnectedNodes(
            List<NodeDefinition> nodes,
            Set<EdgeKey> edges,
            List<String> warnings) {
        if (nodes.isEmpty()) return;
        Map<String, Set<String>> adjacency = new HashMap<>();
        for (NodeDefinition node : nodes) adjacency.put(node.getId(), new HashSet<String>());
        for (EdgeKey edge : edges) {
            adjacency.get(edge.getFirst()).add(edge.getSecond());
            adjacency.get(edge.getSecond()).add(edge.getFirst());
        }
        Deque<String> queue = new ArrayDeque<>();
        Set<String> reached = new HashSet<>();
        for (NodeDefinition node : nodes) {
            if (node.isStart()) {
                queue.add(node.getId());
                reached.add(node.getId());
            }
        }
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            for (String next : adjacency.get(current)) {
                if (reached.add(next)) queue.addLast(next);
            }
        }
        if (reached.size() != nodes.size()) {
            List<String> disconnected = new ArrayList<>();
            for (NodeDefinition node : nodes) {
                if (!reached.contains(node.getId())) disconnected.add(node.getId());
            }
            warnings.add("Nodes not connected to a start node: " + disconnected
                    + ". This is allowed because events may connect them at runtime.");
        }
    }

    private static void validateNamespacedId(String field, String value, List<String> errors) {
        if (!isNamespacedId(value)) {
            errors.add(field + " must use the form modid:name");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class RawMap {
        int schemaVersion;
        String mapId;
        int mapVersion;
        RawSize size;
        RawViewport viewport;
        Map<String, RawEventPool> eventPools;
        List<RawNode> nodes;
    }

    private static final class RawSize { int width; int height; }
    private static final class RawViewport { int radius; }
    private static final class RawEventPool { String drawMode; List<RawEventEntry> events; }
    private static final class RawEventEntry { String eventId; int weight; }
    private static final class RawIcon { String image; String outline; }
    private static final class RawNode {
        String id;
        int x;
        int y;
        String type;
        boolean start;
        String eventPool;
        List<String> links;
        RawIcon icon;
    }
}
