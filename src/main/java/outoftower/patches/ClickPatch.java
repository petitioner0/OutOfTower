package outoftower.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import outoftower.map.CustomMap;
import outoftower.map.MapPositioner;
import outoftower.map.PlayerPathTracker;
import outoftower.map.nodes.AbstractMapNode;

import java.util.UUID;

@SpirePatch(clz = DungeonMapScreen.class, method = "update")
public class ClickPatch {

    @SpirePostfixPatch
    public static void postfix(DungeonMapScreen __instance){

        if (!AbstractDungeon.screen.equals(AbstractDungeon.CurrentScreen.MAP))
            return;

        if (AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE)
            return;

        // DungeonMapScreen sets clicked only after a complete click (mouse release or
        // controller confirmation). Using InputHelper.justClickedLeft here starts a
        // transition on mouse-down, then lets the vanilla node code start a second
        // transition when the same click is released during the fade.
        if (__instance.clicked) {

            for (UUID id : CustomMap.nativeNodes.keySet()) {

                MapRoomNode rn = CustomMap.nativeNodes.get(id);

                if (rn.hb.hovered) {
                    // This is a custom node, so do not let MapRoomNode consume the
                    // same click on the next frame.
                    __instance.clicked = false;
                    __instance.clickTimer = 0.0F;

                    // waitingOnFadeOut 在第一次房间切换后不会恢复，不能用它阻止重复点击。
                    // 实际淡出期间才需要忽略新的节点选择。
                    if (AbstractDungeon.isFadingOut) {
                        return;
                    }

                    // 检查是否与当前玩家节点连接
                    if (!CustomMap.canReachFromPlayer(id)) {
                        return; // 节点未连接，不允许移动
                    }

                    AbstractMapNode node = CustomMap.nodes.get(id);

                    CustomMap.playerNode = id;
                    AbstractDungeon.firstRoomChosen = true;
                    
                    // 记录玩家访问了这个节点
                    PlayerPathTracker.recordVisit(id);
                    
                    MapPositioner.recalc();

                    if (rn.room == null) {
                        rn.room = node.getRoom();  // 安全，只初始化一次
                    }
                    AbstractDungeon.nextRoom = rn;

                    AbstractDungeon.nextRoomTransitionStart();
                    return;
                }
            }
        }
    }
}
