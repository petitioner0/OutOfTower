package outoftower.map;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.map.nodes.AbstractMapNode;
import outoftower.util.NodeRegistry;

import java.util.*;

public class CustomMap {

    public static final Map<UUID, AbstractMapNode> nodes = new HashMap<>();
    public static final Map<UUID, MapRoomNode> nativeNodes = new HashMap<>();

    public static UUID playerNode = null;

    /** 清理所有只属于当前一局游戏的静态状态。 */
    public static void resetRunState() {
        playerNode = null;
        nodes.clear();
        nativeNodes.clear();
        PlayerPathTracker.clear();
        // AbstractDungeon.nextRoom 是静态字段，放弃游戏后可能仍指向上一局的
        // 自定义房间。新局第一次自动存档会优先读取它，因此必须显式清空。
        AbstractDungeon.nextRoom = null;

        for (AbstractMapNode node : NodeRegistry.getAllNodes()) {
            node.resetRoom();
        }
    }

    public static void init() {

        StaticGraphMapBuilder.build();

        // ⭐ 如果玩家节点已存在（从存档恢复），则跳过设置起始节点
        if (playerNode != null) {
            // 确保地图位置已更新
            MapPositioner.recalc();
            return;
        }

        List<AbstractMapNode> candidates = new ArrayList<>();

        for (AbstractMapNode n : NodeRegistry.getAllNodes()) {
            if (n.isStartCandidate) {
                candidates.add(n);
            }
        }

        if (candidates.isEmpty()) {
            throw new RuntimeException("没有可作为起始点的节点！");
        }

        AbstractMapNode start = candidates.get(
                AbstractDungeon.mapRng.random(candidates.size() - 1)
        );

        playerNode = start.id;

        // 初次定位
        MapPositioner.recalc();
    }

    public static boolean isConnected(UUID node1Id, UUID node2Id) {
        AbstractMapNode node1 = nodes.get(node1Id);
        AbstractMapNode node2 = nodes.get(node2Id);
        
        if (node1 == null || node2 == null) {
            return false;
        }
        
        // 节点类型只能表示允许连接的类别，不能表示两个具体节点是否相邻。
        // 当前地图中的节点都是同一类型，按类型判断会令非邻接节点也可达。
        for (int[] coord : node1.getCoordLinks()) {
            if (coord != null && coord.length >= 2
                    && coord[0] == node2.gx && coord[1] == node2.gy) {
                return true;
            }
        }
        return false;
    }

    public static boolean canReachFromPlayer(UUID targetId) {
        if (playerNode == null) {
            return false;
        }

        // 新局开始时 playerNode 只表示地图的初始定位点，玩家还没有真正进入它。
        // 第一次选择必须进入该节点本身；记录访问后才允许选择相邻节点。
        if (!PlayerPathTracker.hasVisited(playerNode)) {
            return playerNode.equals(targetId);
        }
        return isConnected(playerNode, targetId);
    }

    public static AbstractRoom getCurrentRoom() {
        if (playerNode == null) return null;
        AbstractMapNode node = nodes.get(playerNode);
        if (node == null) return null;
        return node.getRoom();
    }
}
