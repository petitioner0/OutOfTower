package outoftower.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import outoftower.NodeAccess;
import outoftower.map.nodes.AbstractMapNode;
import outoftower.util.NodeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapPositioner {

    public static final int CENTER_COL = 2;
    public static final int CENTER_ROW = 7;

    private static final List<MapEdge> visualEdges = new ArrayList<>();
    private static UUID edgeLayoutCenter;
    private static boolean edgeLayoutPlayerVisited;
    private static boolean visualEdgesRendered;

    /** 新的 MapRoomNode 实例建立后，强制下次定位重建连线。 */
    public static void invalidateEdgeLayout() {
        visualEdges.clear();
        edgeLayoutCenter = null;
        edgeLayoutPlayerVisited = false;
        visualEdgesRendered = false;
    }

    public static void recalc() {
        if (CustomMap.playerNode == null) return;

        AbstractMapNode player = CustomMap.nodes.get(CustomMap.playerNode);
        if (player == null) return;

        for (AbstractMapNode n : CustomMap.nodes.values()) {
            MapRoomNode rn = CustomMap.nativeNodes.get(n.id);
            if (rn == null) continue;

            int dx = n.gx - player.gx;
            int dy = n.gy - player.gy;
            int dist = Math.abs(dx) + Math.abs(dy);

            // 完全隐藏 > 2 格
            if (dist > 2) {
                rn.x = -9999;
                rn.y = -9999;
                rn.color.a = 0f; // 纯视觉，非必须
                continue;
            }

            // ⭐ 只设置“格子坐标”
            rn.x = CENTER_COL + dx;
            rn.y = CENTER_ROW + dy;

            rn.taken = PlayerPathTracker.hasVisited(n.id);

            boolean reachable = CustomMap.canReachFromPlayer(n.id);
            boolean current = n.id.equals(CustomMap.playerNode);

            // recalc() 会在每帧渲染前调用，直接复用现有 Color
            // 对象，避免为每个可视节点持续分配短命对象。
            if (reachable || current) {
                rn.color.set(MapRoomNode.AVAILABLE_COLOR);
            } else {
                rn.color.set(1f, 1f, 1f, 0.25f);
            }

            // 原版本身已能处理悬停放大和离开缩小；只需阻止它把
            // “悬停但不可达”的节点放大。这样平常每帧不会执行反射写入。
            if (!reachable && rn.hb.hovered) {
                NodeAccess.setScale(rn, 0.5f);
            }
        }

        boolean playerVisited = PlayerPathTracker.hasVisited(player.id);
        if (!player.id.equals(edgeLayoutCenter)
                || playerVisited != edgeLayoutPlayerVisited) {
            rebuildVisualEdges(player);
            edgeLayoutCenter = player.id;
            edgeLayoutPlayerVisited = playerVisited;
        }
    }

    private static void rebuildVisualEdges(AbstractMapNode playerNode) {
        visualEdges.clear();

        // 占位边始终只指向节点自身，既保持 hasEdges() 为 true，
        // 又不会让原版把邻居判定为可达节点并启动呼吸缩放。
        for (MapRoomNode node : CustomMap.nativeNodes.values()) {
            node.getEdges().clear();
            node.addEdge(new MapEdge(node.x, node.y, node.x, node.y));
        }

        MapRoomNode playerRN = CustomMap.nativeNodes.get(playerNode.id);
        if (!isVisible(playerRN)) return;

        for (int[] coord : playerNode.getCoordLinks()) {
            if (coord == null || coord.length < 2) continue;

            AbstractMapNode dst = NodeRegistry.coordMap.get(coord[0] + "," + coord[1]);
            if (dst == null || !CustomMap.canReachFromPlayer(dst.id)) continue;

            MapRoomNode dstRN = CustomMap.nativeNodes.get(dst.id);
            if (!isVisible(dstRN)) continue;

            MapEdge edge = new MapEdge(
                    playerRN.x, playerRN.y, playerRN.offsetX, playerRN.offsetY,
                    dstRN.x, dstRN.y, dstRN.offsetX, dstRN.offsetY,
                    false
            );
            edge.color = MapRoomNode.AVAILABLE_COLOR;
            visualEdges.add(edge);
        }
    }

    public static void beginRender() {
        visualEdgesRendered = false;
    }

    /** 由本帧第一个 MapRoomNode 在自身渲染前调用。 */
    public static void renderEdgesOnce(SpriteBatch sb) {
        if (visualEdgesRendered) return;

        visualEdgesRendered = true;
        for (MapEdge edge : visualEdges) {
            edge.render(sb);
        }
    }

    private static boolean isVisible(MapRoomNode node) {
        return node != null && node.x != -9999 && node.y != -9999;
    }
}
