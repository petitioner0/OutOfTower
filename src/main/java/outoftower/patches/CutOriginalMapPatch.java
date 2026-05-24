package outoftower.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import outoftower.map.CustomMap;

import java.util.ArrayList;

@SpirePatch(clz = AbstractDungeon.class, method = "generateMap")
public class CutOriginalMapPatch {
    @SpirePostfixPatch
    public static void postfix() {

        AbstractDungeon.map = new ArrayList<>();

        CustomMap.init();

    }
}
