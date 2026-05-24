package outoftower.util;

import outoftower.map.OutOfTowerSaveData;

import java.util.HashMap;

public class OOTSave {

    private static HashMap<String, Object> data() {
        return OutOfTowerSaveData.data;
    }

    /* ---------- 基础写入 ---------- */

    public static void set(String key, Object value) {
        data().put(key, value);
    }

    public static void remove(String key) {
        data().remove(key);
    }

    public static boolean has(String key) {
        return data().containsKey(key);
    }

    /* ---------- 布尔值 ---------- */

    public static boolean getBool(String key, boolean def) {
        Object v = data().get(key);
        if (v == null) return def;
        return (boolean) v;
    }

    /* ---------- 整数（注意：Gson 会用 Double 解析） ---------- */

    public static int getInt(String key, int def) {
        Object v = data().get(key);
        if (v == null) return def;

        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Double) return ((Double) v).intValue();

        return def;
    }

    public static void addInt(String key, int delta) {
        int now = getInt(key, 0);
        set(key, now + delta);
    }

    /* ---------- 小数 ---------- */

    public static double getDouble(String key, double def) {
        Object v = data().get(key);
        if (v == null) return def;

        if (v instanceof Double) return (Double) v;
        if (v instanceof Integer) return ((Integer) v).doubleValue();

        return def;
    }

    /* ---------- 字符串 ---------- */

    public static String getString(String key, String def) {
        Object v = data().get(key);
        if (v == null) return def;
        return (String) v;
    }

    /* ---------- 命名空间帮助函数 ---------- */

    public static String ns(String... parts) {
        return String.join(".", parts);
    }

    /* ---------- 清空 ---------- */

    public static void clear() {
        data().clear();
    }
}
