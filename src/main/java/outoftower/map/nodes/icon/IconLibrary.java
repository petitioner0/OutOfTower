package outoftower.map.nodes.icon;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.util.HashMap;

public class IconLibrary {

    private static HashMap<IconType, Texture> icons = new HashMap<>();
    private static HashMap<IconType, Texture> outlines = new HashMap<>();

    static {
        // ===== 原版图标 =====
        icons.put(IconType.MONSTER, ImageMaster.MAP_NODE_ENEMY);
        outlines.put(IconType.MONSTER, ImageMaster.MAP_NODE_ENEMY_OUTLINE);

        icons.put(IconType.ELITE, ImageMaster.MAP_NODE_ELITE);
        outlines.put(IconType.ELITE, ImageMaster.MAP_NODE_ELITE_OUTLINE);

        icons.put(IconType.EVENT, ImageMaster.MAP_NODE_EVENT);
        outlines.put(IconType.EVENT, ImageMaster.MAP_NODE_EVENT_OUTLINE);

        icons.put(IconType.TREASURE, ImageMaster.MAP_NODE_TREASURE);
        outlines.put(IconType.TREASURE, ImageMaster.MAP_NODE_TREASURE_OUTLINE);

        icons.put(IconType.SHOP, ImageMaster.MAP_NODE_MERCHANT);
        outlines.put(IconType.SHOP, ImageMaster.MAP_NODE_MERCHANT_OUTLINE);

        icons.put(IconType.REST, ImageMaster.MAP_NODE_REST);
        outlines.put(IconType.REST, ImageMaster.MAP_NODE_REST_OUTLINE);

        // ====== 自定义图标 ======
        addCustomIcon(IconType.STORY_EVENT, "img/map/story.png", "img/map/story_outline.png");
        addCustomIcon(IconType.TRAP_EVENT, "img/map/trap.png",  "img/map/trap_outline.png");
        addCustomIcon(IconType.SPECIAL_NODE, "img/map/special.png", "img/map/special_outline.png");
    }

    public static void addCustomIcon(IconType type, String imgPath, String outlinePath) {
        icons.put(type, ImageMaster.loadImage(imgPath));
        outlines.put(type, ImageMaster.loadImage(outlinePath));
    }

    public static Texture getIcon(IconType type) {
        return icons.get(type);
    }

    public static Texture getOutline(IconType type) {
        return outlines.get(type);
    }
}
