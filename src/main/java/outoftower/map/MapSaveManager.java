package outoftower.map;

import basemod.abstracts.CustomSavable;
import outoftower.map.runtime.MapManager;
import outoftower.map.save.MapSaveDataV1;

/** BaseMod adapter; all save ordering concerns are handled by MapManager. */
public final class MapSaveManager implements CustomSavable<MapSaveDataV1> {
    @Override
    public MapSaveDataV1 onSave() {
        return MapManager.save();
    }

    @Override
    public void onLoad(MapSaveDataV1 data) {
        MapManager.load(data);
    }
}
