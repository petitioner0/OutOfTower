package outoftower.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import outoftower.map.runtime.MapManager;

@SpirePatch(clz = AbstractDungeon.class, method = "generateMap")
public class CutOriginalMapPatch {
    @SpirePostfixPatch
    public static void postfix() {
        // No registered content means a completely vanilla map. MapManager also
        // waits for pending save data without consuming mapRng during load.
        MapManager.activateForCurrentAct();
    }
}
