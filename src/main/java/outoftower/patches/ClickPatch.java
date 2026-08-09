package outoftower.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import outoftower.map.MapPositioner;
import outoftower.map.runtime.MapManager;
import outoftower.map.runtime.MapSession;

import java.util.Map;

@SpirePatch(clz = DungeonMapScreen.class, method = "update")
public class ClickPatch {
    @SpirePostfixPatch
    public static void postfix(DungeonMapScreen screen) {
        MapSession session = MapManager.getSession();
        if (session == null || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.MAP) return;
        if (AbstractDungeon.getCurrRoom() == null
                || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE) return;
        if (!screen.clicked) return;

        for (Map.Entry<String, MapRoomNode> entry : session.getNativeNodes().entrySet()) {
            String nodeId = entry.getKey();
            MapRoomNode nativeNode = entry.getValue();
            if (!nativeNode.hb.hovered) continue;

            // Consume the click before vanilla MapRoomNode can start a second transition.
            screen.clicked = false;
            screen.clickTimer = 0.0F;
            if (AbstractDungeon.isFadingOut || !session.canReach(nodeId)) return;

            session.moveTo(nodeId);
            AbstractDungeon.firstRoomChosen = true;
            MapPositioner.recalc();
            AbstractDungeon.nextRoom = nativeNode;
            AbstractDungeon.nextRoomTransitionStart();
            return;
        }
    }
}
