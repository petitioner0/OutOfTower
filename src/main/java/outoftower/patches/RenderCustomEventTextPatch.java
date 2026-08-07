package outoftower.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CtBehavior;
import outoftower.map.nodes.room.OutOfTowerEventRoom;

/**
 * AbstractDungeon.render 只会为原版 EventRoom 绘制事件文本和选项。
 * 自定义事件房不能继承 EventRoom（否则转场时会被随机问号房替换），
 * 因此在原版取得当前房间、判断 EventRoom 之前补上这次渲染。
 */
@SpirePatch(clz = AbstractDungeon.class, method = "render")
public class RenderCustomEventTextPatch {

    @SpireInsertPatch(locator = Locator.class)
    public static void insert(AbstractDungeon __instance, SpriteBatch sb) {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room instanceof OutOfTowerEventRoom) {
            room.renderEventTexts(sb);
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(
                    AbstractDungeon.class,
                    "getCurrRoom"
            );
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}
