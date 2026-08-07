package outoftower;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.MapRoomNode;

public class NodeAccess {

    public static float getScale(MapRoomNode node) {
        try {
            return ReflectionHacks.getPrivate(
                    node,
                    MapRoomNode.class,
                    "scale"
            );
        } catch (Exception e) {
            return Settings.scale;
        }
    }

    public static void setScale(MapRoomNode node, float scale) {
        try {
            ReflectionHacks.setPrivate(
                    node,
                    MapRoomNode.class,
                    "scale",
                    scale
            );
        } catch (Exception ignored) {
            // 不应因为视觉缩放字段变更中断地图渲染。
        }
    }

}
