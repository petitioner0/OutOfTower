package outoftower.map;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.MapRoomNode;
import outoftower.map.nodes.AbstractMapNode;

public class MapPositioner {

    public static final int CENTER_COL = 2;
    public static final int CENTER_ROW = 7;


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

            boolean connected = CustomMap.isConnected(CustomMap.playerNode, n.id);
            boolean current = n.id.equals(CustomMap.playerNode);

            rn.color = (connected || current)
                    ? MapRoomNode.AVAILABLE_COLOR.cpy()
                    : new Color(1, 1, 1, 0.25f);
        }
    }
}