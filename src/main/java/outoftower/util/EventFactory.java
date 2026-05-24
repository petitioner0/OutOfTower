package outoftower.util;

import com.megacrit.cardcrawl.events.AbstractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EventFactory {
    private static final Map<String, Supplier<AbstractEvent>> registry = new HashMap<>();

    public static void register(String id, Supplier<AbstractEvent> ctor) {
        registry.put(id, ctor);
    }

    public static AbstractEvent create(String id) {
        Supplier<AbstractEvent> ctor = registry.get(id);
        if (ctor == null) {
            throw new RuntimeException("Unknown event id: " + id);
        }
        return ctor.get();
    }
}
