package outoftower.map;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.map.nodes.AbstractMapNode;
import outoftower.map.nodes.icon.IconLibrary;
import outoftower.map.nodes.icon.IconType;
import outoftower.map.nodes.room.OutOfTowerEventRoom;
import outoftower.patches.NodeIconFields;
import outoftower.util.NodeRegistry;

import java.util.ArrayList;

public class StaticGraphMapBuilder {



    public static void build() {

        CustomMap.nodes.clear();
        CustomMap.nativeNodes.clear();
        MapPositioner.invalidateEdgeLayout();

        // ⭐ 1. 先让所有节点生成房间 + 预先抽事件！
        for (AbstractMapNode n : NodeRegistry.getAllNodes()) {
            n.initRoom();  // ← 房间与事件在地图生成阶段就创建好
        }

        // 2. MapRoomNode + 绑定房间 + 初始化事件 + 图标渲染
        for (AbstractMapNode n : NodeRegistry.getAllNodes()) {

            MapRoomNode rn = new MapRoomNode(n.gx, n.gy);

            AbstractRoom room = n.getRoom();
            rn.room = room;

            // ⭐ 初始化事件 + 渲染图标
            if (room instanceof OutOfTowerEventRoom) {
                OutOfTowerEventRoom er = (OutOfTowerEventRoom) room;

                er.initEventIfNeeded();       // ←← 必须在这里初始化事件（抽池）

                IconType type = er.getIconType();
                NodeIconFields.customIcon.set(rn, IconLibrary.getIcon(type));
                NodeIconFields.customOutline.set(rn, IconLibrary.getOutline(type));
            }

            CustomMap.nodes.put(n.id, n);
            CustomMap.nativeNodes.put(n.id, rn);
        }

        // DungeonMapScreen.updateImage() 只会收集 hasEdges() 的节点。
        // 四参数 MapEdge 不会生成 MapDot，因此只用作节点的无渲染占位边。
        // 真正可见的可达路径由 MapPositioner 独立管理，避免触发
        // MapRoomNode.update() 内置的可达动画和点击逻辑。
        for (AbstractMapNode n : NodeRegistry.getAllNodes()) {
            MapRoomNode rn = CustomMap.nativeNodes.get(n.id);
            rn.addEdge(new MapEdge(rn.x, rn.y, rn.x, rn.y));
        }

        // ⭐ 4. 构建 AbstractDungeon.map 的二维结构
        AbstractDungeon.map = new ArrayList<>();

        // 计算最大行数（确保包含所有节点）
        int maxRow = 0;
        for (AbstractMapNode n : NodeRegistry.getAllNodes()) {
            if (n.gy > maxRow) {
                maxRow = n.gy;
            }
        }
        
        // 确保至少创建足够的行
        maxRow = Math.max(maxRow, 11);
        
        for (int i = 0; i <= maxRow; i++) {
            AbstractDungeon.map.add(new ArrayList<>());
        }

        for (AbstractMapNode n : NodeRegistry.getAllNodes()) {
            // ⚠️ 安全检查：确保 gy 在有效范围内
            if (n.gy >= 0 && n.gy <= maxRow) {
                AbstractDungeon.map.get(n.gy).add(CustomMap.nativeNodes.get(n.id));
            }
        }
    }
}
