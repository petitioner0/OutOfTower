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

    public static float getAngle(MapRoomNode node) {
        try {
            return ReflectionHacks.getPrivate(
                    node,
                    MapRoomNode.class,
                    "angle"
            );
        } catch (Exception e) {
            return 0f;
        }
    }
}