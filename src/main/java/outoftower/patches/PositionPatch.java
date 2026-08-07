package outoftower.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import outoftower.map.MapPositioner;

@SpirePatch(clz = DungeonMapScreen.class, method = "render")
public class PositionPatch {
    @SpirePrefixPatch
    public static void prefix(DungeonMapScreen __instance){
        MapPositioner.recalc();
        MapPositioner.beginRender();
    }
}
