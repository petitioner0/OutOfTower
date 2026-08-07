package outoftower.util;

import outoftower.map.nodes.AbstractMapNode;
import outoftower.map.nodes.wildnode.WildEventNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NodeRegistry {

    public static final Map<String, AbstractMapNode> coordMap = new HashMap<>();

    static {
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                register(new WildEventNode(x, y));
            }
        }
    }

    public static void register(AbstractMapNode n) {
        coordMap.put(n.gx + "," + n.gy, n);
    }

    // 获取所有注册的节点实例（用于测试多个相同类的节点）
    public static Collection<AbstractMapNode> getAllNodes() {
        return coordMap.values();
    }
}
