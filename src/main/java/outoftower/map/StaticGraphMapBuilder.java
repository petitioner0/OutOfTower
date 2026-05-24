package outoftower.map;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.Legend;
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

        // ⭐ 3. 建立节点之间的连线
        // MapEdge 构造函数需要显示坐标（x, y），用于在地图中查找节点
        // 由于节点的 x/y 会在 MapPositioner.recalc() 中动态更新，
        // 我们先用 gx/gy 初始化 x/y，建立连线后再由 MapPositioner 更新
        for (AbstractMapNode n : NodeRegistry.getAllNodes()) {
            MapRoomNode rn = CustomMap.nativeNodes.get(n.id);
            // 初始化显示坐标为逻辑坐标（后续会被 MapPositioner 更新）
            rn.x = n.gx;
            rn.y = n.gy;
        }

        // 建立连线：遍历所有节点，通过坐标查找邻居
        for (AbstractMapNode src : NodeRegistry.getAllNodes()) {
            MapRoomNode srcRN = CustomMap.nativeNodes.get(src.id);

            // 通过坐标链接查找邻居节点
            for (int[] coord : src.getCoordLinks()) {
                String key = coord[0] + "," + coord[1];
                AbstractMapNode dst = NodeRegistry.coordMap.get(key);
                if (dst != null && CustomMap.nativeNodes.containsKey(dst.id)) {
                    MapRoomNode dstRN = CustomMap.nativeNodes.get(dst.id);
                    // ⭐ 使用当前的显示坐标建立连线
                    // MapEdge 在渲染时会使用节点的实际位置（hb.cX/cY）
                    srcRN.addEdge(new MapEdge(srcRN.x, srcRN.y, dstRN.x, dstRN.y));
                }
            }
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
