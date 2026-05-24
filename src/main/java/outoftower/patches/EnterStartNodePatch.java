package outoftower.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import outoftower.map.CustomMap;

@SpirePatch(clz = AbstractDungeon.class, method = "dungeonTransitionSetup")
public class EnterStartNodePatch {
    private static int lastHandledAct = -1;

    @SpirePostfixPatch
    public static void postfix() {

        if (lastHandledAct == AbstractDungeon.actNum)
            return;

        lastHandledAct = AbstractDungeon.actNum;

        if (CustomMap.playerNode == null)
            return;

        MapRoomNode rn = CustomMap.nativeNodes.get(CustomMap.playerNode);
        if (rn == null || rn.room == null)
            return;

        AbstractDungeon.nextRoom = rn;
        AbstractDungeon.nextRoomTransitionStart();
    }
}