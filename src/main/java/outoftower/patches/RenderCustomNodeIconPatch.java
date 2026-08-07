package outoftower.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.MapRoomNode;
import outoftower.NodeAccess;
import outoftower.map.MapPositioner;

@SpirePatch(clz = MapRoomNode.class, method = "render")
public class RenderCustomNodeIconPatch {

    // 与 MapRoomNode 原版轮廓颜色保持一致：默认灰色，悬停时变亮。
    private static final Color OUTLINE_COLOR = Color.valueOf("8c8c80ff");
    private static final Color HOVERED_OUTLINE_COLOR = new Color(0.9f, 0.9f, 0.9f, 1f);

    @SpirePrefixPatch
    public static void prefix(MapRoomNode node, SpriteBatch sb) {
        // DungeonMapScreen 已经完成背景渲染，但尚未渲染任何节点。
        // 每帧只在第一个节点前绘制一次自定义路径。
        MapPositioner.renderEdgesOnce(sb);
    }

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

        sb.setColor(node.hb != null && node.hb.hovered
                ? HOVERED_OUTLINE_COLOR
                : OUTLINE_COLOR);
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
