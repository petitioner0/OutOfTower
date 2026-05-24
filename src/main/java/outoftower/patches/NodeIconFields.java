package outoftower.patches;

import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.map.MapRoomNode;

@SpirePatch(clz = MapRoomNode.class, method = SpirePatch.CLASS)
public class NodeIconFields {
    public static SpireField<Texture> customIcon = new SpireField<>(() -> null);
    public static SpireField<Texture> customOutline = new SpireField<>(() -> null);
}