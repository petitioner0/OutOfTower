package outoftower.api;

import outoftower.map.runtime.ContentRegistry;
import outoftower.map.runtime.MapManager;

import java.util.Optional;

/** Stable public entry point for content mods. */
public final class OutOfTowerApi {
    private OutOfTowerApi() {
    }

    public static void registerContent(ContentPack contentPack) {
        ContentRegistry.register(contentPack);
    }

    public static Optional<MapRuntime> getCurrentMap() {
        return MapManager.getRuntime();
    }
}
