package outoftower.map.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import outoftower.map.definition.IconDefinition;

import java.util.HashMap;
import java.util.Map;

public final class MapIconCache {
    private static final Map<String, Texture> CACHE = new HashMap<>();

    private MapIconCache() {
    }

    public static TexturePair load(IconDefinition definition) {
        if (definition == null
                || !Gdx.files.internal(definition.getImagePath()).exists()
                || !Gdx.files.internal(definition.getOutlinePath()).exists()) {
            return new TexturePair(ImageMaster.MAP_NODE_EVENT, ImageMaster.MAP_NODE_EVENT_OUTLINE, false);
        }
        return new TexturePair(
                loadOne(definition.getImagePath()),
                loadOne(definition.getOutlinePath()),
                true);
    }

    private static Texture loadOne(String path) {
        Texture texture = CACHE.get(path);
        if (texture == null) {
            texture = ImageMaster.loadImage(path);
            CACHE.put(path, texture);
        }
        return texture;
    }

    public static final class TexturePair {
        public final Texture image;
        public final Texture outline;
        public final boolean custom;

        TexturePair(Texture image, Texture outline, boolean custom) {
            this.image = image;
            this.outline = outline;
            this.custom = custom;
        }
    }
}
