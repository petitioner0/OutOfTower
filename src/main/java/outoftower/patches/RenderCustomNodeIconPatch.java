package outoftower.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.MapRoomNode;
import outoftower.NodeAccess;

@SpirePatch(clz = MapRoomNode.class, method = "render")
public class RenderCustomNodeIconPatch {

    @SpirePostfixPatch
    public static void postfix(MapRoomNode node, SpriteBatch sb) {

        Texture icon = NodeIconFields.customIcon.get(node);
        Texture outline = NodeIconFields.customOutline.get(node);
        if (icon == null || outline == null) return;

        float cx = node.hb.cX;
        float cy = node.hb.cY;

        float drawX = cx - 64f;
        float drawY = cy - 64f;

        float scale = NodeAccess.getScale(node) * Settings.scale;

        sb.setColor(Color.WHITE);
        sb.draw(outline, drawX, drawY,
                64, 64, 128, 128,
                scale, scale, 0,
                0, 0, 128, 128,
                false, false);

        sb.setColor(node.color);
        sb.draw(icon, drawX, drawY,
                64, 64, 128, 128,
                scale, scale, 0,
                0, 0, 128, 128,
                false, false);
    }
}