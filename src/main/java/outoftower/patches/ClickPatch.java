package outoftower.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
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

        if (InputHelper.justClickedLeft) {

            for (UUID id : CustomMap.nativeNodes.keySet()) {

                MapRoomNode rn = CustomMap.nativeNodes.get(id);

                if (rn.hb.hovered) {

                    // 检查是否与当前玩家节点连接
                    if (!CustomMap.canReachFromPlayer(id)) {
                        return; // 节点未连接，不允许移动
                    }

                    AbstractMapNode node = CustomMap.nodes.get(id);

                    CustomMap.playerNode = id;
                    
                    // 记录玩家访问了这个节点
                    PlayerPathTracker.recordVisit(id);
                    
                    MapPositioner.recalc();

                    MapRoomNode realNode = CustomMap.nativeNodes.get(id);
                    if (realNode.room == null) {
                        realNode.room = node.getRoom();  // 安全，只初始化一次
                    }
                    AbstractDungeon.nextRoom = realNode;

                    AbstractDungeon.closeCurrentScreen();
                    AbstractDungeon.nextRoomTransitionStart();
                    return;
                }
            }
        }
    }
}