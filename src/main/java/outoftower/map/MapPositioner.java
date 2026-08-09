package outoftower.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import outoftower.NodeAccess;
import outoftower.map.definition.EdgeKey;
import outoftower.map.definition.NodeDefinition;
import outoftower.map.runtime.MapManager;
import outoftower.map.runtime.MapSession;

import java.util.ArrayList;
import java.util.List;

public final class MapPositioner {
    private static final int CENTER_COL = 3;
    private static final int CENTER_ROW = 7;
    private static final int HIDDEN_COORDINATE = -9999;

    private static final List<MapEdge> visualEdges = new ArrayList<>();
    private static String edgeLayoutCenter;
    private static long edgeLayoutRevision = Long.MIN_VALUE;
    private static boolean visualEdgesRendered;

    private MapPositioner() {
    }

    public static void invalidateEdgeLayout() {
        visualEdges.clear();
        edgeLayoutCenter = null;
        edgeLayoutRevision = Long.MIN_VALUE;
        visualEdgesRendered = false;
    }

    public static void recalc() {
        MapSession session = MapManager.getSession();
        if (session == null || session.getCurrentNodeId() == null) return;
        NodeDefinition player = session.getDefinition().getNodesById().get(session.getCurrentNodeId());
        if (player == null) return;

        for (NodeDefinition node : session.getDefinition().getNodes()) {
            MapRoomNode nativeNode = session.getNativeNodes().get(node.getId());
            if (nativeNode == null) continue;

            int dx = node.getX() - player.getX();
            int dy = node.getY() - player.getY();
            int distance = Math.abs(dx) + Math.abs(dy);
            if (distance > session.getDefinition().getViewportRadius()) {
                nativeNode.x = HIDDEN_COORDINATE;
                nativeNode.y = HIDDEN_COORDINATE;
                nativeNode.color.a = 0f;
                continue;
            }

            nativeNode.x = CENTER_COL + dx;
            nativeNode.y = CENTER_ROW + dy;
            nativeNode.taken = session.hasVisited(node.getId());
            boolean reachable = session.canReach(node.getId());
            boolean current = node.getId().equals(session.getCurrentNodeId());
            if (reachable || current) nativeNode.color.set(MapRoomNode.AVAILABLE_COLOR);
            else nativeNode.color.set(1f, 1f, 1f, 0.25f);

            if (!reachable && nativeNode.hb.hovered) NodeAccess.setScale(nativeNode, 0.5f);
        }

        if (!session.getCurrentNodeId().equals(edgeLayoutCenter)
                || edgeLayoutRevision != session.getVisualRevision()) {
            rebuildVisualEdges(session);
            edgeLayoutCenter = session.getCurrentNodeId();
            edgeLayoutRevision = session.getVisualRevision();
        }
    }

    private static void rebuildVisualEdges(MapSession session) {
        visualEdges.clear();
        for (MapRoomNode node : session.getNativeNodes().values()) {
            node.getEdges().clear();
            // DungeonMapScreen only includes nodes with at least one native edge.
            node.addEdge(new MapEdge(node.x, node.y, node.x, node.y));
        }

        for (EdgeKey edgeKey : session.getEffectiveEdges()) {
            MapRoomNode first = session.getNativeNodes().get(edgeKey.getFirst());
            MapRoomNode second = session.getNativeNodes().get(edgeKey.getSecond());
            if (!isVisible(first) || !isVisible(second)) continue;
            MapEdge edge = new MapEdge(
                    first.x, first.y, first.offsetX, first.offsetY,
                    second.x, second.y, second.offsetX, second.offsetY,
                    false);
            if (edgeKey.contains(session.getCurrentNodeId())) {
                edge.color = MapRoomNode.AVAILABLE_COLOR;
            }
            visualEdges.add(edge);
        }
    }

    public static void beginRender() {
        visualEdgesRendered = false;
    }

    public static void renderEdgesOnce(SpriteBatch sb) {
        if (visualEdgesRendered || MapManager.getSession() == null) return;
        visualEdgesRendered = true;
        for (MapEdge edge : visualEdges) edge.render(sb);
    }

    private static boolean isVisible(MapRoomNode node) {
        return node != null && node.x != HIDDEN_COORDINATE && node.y != HIDDEN_COORDINATE;
    }
}
