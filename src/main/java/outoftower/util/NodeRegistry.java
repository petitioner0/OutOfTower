package outoftower.util;

import outoftower.map.nodes.AbstractMapNode;
import outoftower.map.nodes.wildnode.WildEventNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeRegistry {

    public static final Map<Class<? extends AbstractMapNode>, AbstractMapNode> nodes = new LinkedHashMap<>();
    public static final Map<String, AbstractMapNode> coordMap = new HashMap<>();

    static {
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                register(new WildEventNode(x, y));
            }
        }

        buildConnections();
    }

    public static void register(AbstractMapNode n) {
        nodes.put(n.getClass(), n);
        coordMap.put(n.gx + "," + n.gy, n);
    }

    // 获取所有注册的节点实例（用于测试多个相同类的节点）
    public static Collection<AbstractMapNode> getAllNodes() {
        return coordMap.values();
    }

    // ★ 坐标查询邻接点 → 转换为类邻接点
    private static void buildConnections() {

        // 遍历 coordMap 中的所有节点，而不是 nodes（因为 nodes 中同一类只保留最后一个实例）
        for (AbstractMapNode node : coordMap.values()) {

            for (int[] c : node.getCoordLinks()) {

                String key = c[0] + "," + c[1];

                if (!coordMap.containsKey(key))
                    continue; // 没找到该坐标的节点 → 忽略

                AbstractMapNode target = coordMap.get(key);

                // 双向连接（无向图）
                node.neighbors.add(target.getClass());
                target.neighbors.add(node.getClass());
            }
        }
    }
}
