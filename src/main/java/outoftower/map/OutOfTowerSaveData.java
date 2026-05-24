package outoftower.map;

import basemod.abstracts.CustomSavable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class OutOfTowerSaveData implements CustomSavable<String> {
    public static HashMap<String, Object> data = new HashMap<>();

    @Override
    public String onSave() {
        return new Gson().toJson(data);
    }

    @Override
    public void onLoad(String json) {
        data.clear();
        if (json != null) {
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            data = new Gson().fromJson(json, type);
        }
    }
}