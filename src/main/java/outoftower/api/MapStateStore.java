package outoftower.api;

import com.google.gson.JsonElement;

/** Namespaced, JSON-backed state that is persisted with the active map. */
public interface MapStateStore {
    JsonElement get(String namespace, String key);
    void put(String namespace, String key, JsonElement value);
    JsonElement remove(String namespace, String key);
    boolean contains(String namespace, String key);
}
