package outoftower.api;

import com.megacrit.cardcrawl.events.AbstractEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A content mod's complete OutOfTower registration.
 *
 * <p>Map topology remains JSON-only. This object only associates Act numbers
 * with JSON resources and event ids with constructors.</p>
 */
public final class ContentPack {
    private final String ownerId;
    private final Map<Integer, String> actMaps;
    private final Map<String, Supplier<? extends AbstractEvent>> eventFactories;

    private ContentPack(Builder builder) {
        this.ownerId = builder.ownerId;
        this.actMaps = Collections.unmodifiableMap(new LinkedHashMap<>(builder.actMaps));
        this.eventFactories = Collections.unmodifiableMap(new LinkedHashMap<>(builder.eventFactories));
    }

    public static Builder builder(String ownerId) {
        return new Builder(ownerId);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Map<Integer, String> getActMaps() {
        return actMaps;
    }

    public Map<String, Supplier<? extends AbstractEvent>> getEventFactories() {
        return eventFactories;
    }

    public static final class Builder {
        private final String ownerId;
        private final Map<Integer, String> actMaps = new LinkedHashMap<>();
        private final Map<String, Supplier<? extends AbstractEvent>> eventFactories = new LinkedHashMap<>();

        private Builder(String ownerId) {
            if (ownerId == null || ownerId.trim().isEmpty()) {
                throw new IllegalArgumentException("ownerId must not be blank");
            }
            this.ownerId = ownerId.trim();
        }

        public Builder actMap(int actIndex, String resourcePath) {
            if (actIndex < 1) {
                throw new IllegalArgumentException("actIndex must be at least 1");
            }
            if (resourcePath == null || resourcePath.trim().isEmpty()) {
                throw new IllegalArgumentException("resourcePath must not be blank");
            }
            if (actMaps.put(actIndex, resourcePath.trim()) != null) {
                throw new IllegalArgumentException("Duplicate map for Act " + actIndex);
            }
            return this;
        }

        public Builder event(String eventId, Supplier<? extends AbstractEvent> factory) {
            if (eventId == null || eventId.trim().isEmpty()) {
                throw new IllegalArgumentException("eventId must not be blank");
            }
            if (factory == null) {
                throw new IllegalArgumentException("factory must not be null");
            }
            String normalizedId = eventId.trim();
            if (eventFactories.put(normalizedId, factory) != null) {
                throw new IllegalArgumentException("Duplicate event id: " + normalizedId);
            }
            return this;
        }

        public ContentPack build() {
            if (actMaps.isEmpty()) {
                throw new IllegalStateException("A content pack must register at least one Act map");
            }
            return new ContentPack(this);
        }
    }
}
