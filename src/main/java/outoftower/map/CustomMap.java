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
    public static boolean shouldEnterStartNode;

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
        
        // 记录起始节点
        PlayerPathTracker.recordVisit(playerNode);

        // 初次定位
        MapPositioner.recalc();
    }

    public static boolean isConnected(UUID node1Id, UUID node2Id) {
        AbstractMapNode node1 = nodes.get(node1Id);
        AbstractMapNode node2 = nodes.get(node2Id);
        
        if (node1 == null || node2 == null) {
            return false;
        }
        
        // 检查 node1 的邻居中是否包含 node2 的类
        return node1.neighbors.contains(node2.getClass());
    }

    public static boolean canReachFromPlayer(UUID targetId) {
        if (playerNode == null) {
            return false;
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