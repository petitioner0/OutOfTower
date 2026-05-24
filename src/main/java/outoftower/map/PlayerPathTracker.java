package outoftower.map;

import basemod.abstracts.CustomSavable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import outoftower.util.PlayerPathData;

import java.lang.reflect.Type;
import java.util.*;

public class PlayerPathTracker implements CustomSavable<String> {

    private static final Gson gson = new Gson();

    private static final List<UUID> visited = new ArrayList<>();
    private static final Map<UUID, Integer> visitCount = new HashMap<>();

    public static void recordVisit(UUID id) {
        if(!visited.contains(id)) {
            visited.add(id);
        }
        visitCount.put(id, visitCount.getOrDefault(id, 0) + 1);
    }

    public static boolean hasVisited(UUID id) {
        return visited.contains(id);
    }

    public static void clear() {
        visited.clear();
        visitCount.clear();
    }

    /** ========== 序列化到存档 ========== */
    @Override
    public String onSave() {
        PlayerPathData data = new PlayerPathData();

        for(UUID u : visited) {
            data.visited.add(u.toString());
        }
        for(Map.Entry<UUID,Integer> e : visitCount.entrySet()) {
            data.count.put(e.getKey().toString(), e.getValue());
        }
        return gson.toJson(data);
    }

    /** ========== 从存档恢复 ========== */
    @Override
    public void onLoad(String json) {
        clear();
        if(json == null) return;

        Type type = new TypeToken<PlayerPathData>(){}.getType();
        PlayerPathData data = gson.fromJson(json, type);

        if(data == null) return;

        for(String s : data.visited) {
            visited.add(UUID.fromString(s));
        }
        for(Map.Entry<String,Integer> e : data.count.entrySet()) {
            visitCount.put(UUID.fromString(e.getKey()), e.getValue());
        }
    }
}
