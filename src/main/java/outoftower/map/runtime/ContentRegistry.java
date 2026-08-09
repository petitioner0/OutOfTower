package outoftower.map.runtime;

import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.events.AbstractEvent;
import outoftower.api.ContentPack;
import outoftower.api.ContentRegistrationException;
import outoftower.map.definition.IconDefinition;
import outoftower.map.definition.MapDefinition;
import outoftower.map.definition.MapDefinitionLoader;
import outoftower.map.definition.MapLoadResult;
import outoftower.map.definition.MapValidationException;
import outoftower.map.definition.NodeDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Process-wide registry populated by the single active content mod. */
public final class ContentRegistry {
    private static ContentPack contentPack;
    private static Map<Integer, MapDefinition> mapsByAct = Collections.emptyMap();
    private static Map<String, Supplier<? extends AbstractEvent>> eventFactories = Collections.emptyMap();

    private ContentRegistry() {
    }

    public static synchronized void register(ContentPack pack) {
        if (pack == null) throw new IllegalArgumentException("contentPack must not be null");
        if (contentPack != null) {
            List<String> errors = new ArrayList<>();
            errors.add("Only one content pack may be active. Already registered: "
                    + contentPack.getOwnerId() + "; attempted: " + pack.getOwnerId());
            throw new ContentRegistrationException(errors);
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        LinkedHashMap<Integer, MapDefinition> compiledMaps = new LinkedHashMap<>();
        Map<String, Integer> mapIds = new LinkedHashMap<>();

        for (String eventId : pack.getEventFactories().keySet()) {
            if (!MapDefinitionLoader.isNamespacedId(eventId)) {
                errors.add("Registered event id must use the form modid:name: " + eventId);
            }
        }

        for (Map.Entry<Integer, String> entry : pack.getActMaps().entrySet()) {
            int actIndex = entry.getKey();
            String resourcePath = entry.getValue();
            try {
                if (!Gdx.files.internal(resourcePath).exists()) {
                    errors.add("Act " + actIndex + " map resource does not exist: " + resourcePath);
                    continue;
                }
                String json = Gdx.files.internal(resourcePath).readString("UTF-8");
                MapLoadResult result = MapDefinitionLoader.parse(json, pack.getEventFactories().keySet());
                MapDefinition definition = result.getDefinition();
                Integer previousAct = mapIds.put(definition.getMapId(), actIndex);
                if (previousAct != null) {
                    errors.add("mapId " + definition.getMapId() + " is used for both Act "
                            + previousAct + " and Act " + actIndex);
                }
                compiledMaps.put(actIndex, definition);
                for (String warning : result.getWarnings()) {
                    warnings.add(resourcePath + ": " + warning);
                }
                validateIconResources(resourcePath, definition, warnings);
            } catch (MapValidationException exception) {
                for (String error : exception.getErrors()) {
                    errors.add(resourcePath + ": " + error);
                }
            } catch (RuntimeException exception) {
                errors.add(resourcePath + ": could not load map: " + exception.getMessage());
            }
        }

        if (!errors.isEmpty()) throw new ContentRegistrationException(errors);
        contentPack = pack;
        mapsByAct = Collections.unmodifiableMap(compiledMaps);
        eventFactories = Collections.unmodifiableMap(new LinkedHashMap<>(pack.getEventFactories()));
        for (String warning : warnings) logWarning(warning);
    }

    public static boolean hasContent() {
        return contentPack != null;
    }

    public static String getOwnerId() {
        return contentPack == null ? null : contentPack.getOwnerId();
    }

    public static MapDefinition requireMapForAct(int actIndex) {
        if (contentPack == null) return null;
        MapDefinition definition = mapsByAct.get(actIndex);
        if (definition == null) {
            throw new IllegalStateException("OutOfTower content pack " + contentPack.getOwnerId()
                    + " does not define a map for Act " + actIndex);
        }
        return definition;
    }

    public static boolean hasEvent(String eventId) {
        return eventFactories.containsKey(eventId);
    }

    public static AbstractEvent createEvent(String eventId) {
        Supplier<? extends AbstractEvent> factory = eventFactories.get(eventId);
        if (factory == null) throw new IllegalStateException("Unregistered OutOfTower event: " + eventId);
        AbstractEvent event = factory.get();
        if (event == null) throw new IllegalStateException("Event factory returned null for " + eventId);
        return event;
    }

    static synchronized void clearForTests() {
        contentPack = null;
        mapsByAct = Collections.emptyMap();
        eventFactories = Collections.emptyMap();
    }

    private static void validateIconResources(
            String mapResource,
            MapDefinition definition,
            List<String> warnings) {
        for (NodeDefinition node : definition.getNodes()) {
            IconDefinition icon = node.getIcon();
            if (icon == null) continue;
            if (!Gdx.files.internal(icon.getImagePath()).exists()) {
                warnings.add(mapResource + ": node " + node.getId()
                        + " icon image is missing; the default event icon will be used: "
                        + icon.getImagePath());
            }
            if (!Gdx.files.internal(icon.getOutlinePath()).exists()) {
                warnings.add(mapResource + ": node " + node.getId()
                        + " icon outline is missing; the default event icon will be used: "
                        + icon.getOutlinePath());
            }
        }
    }

    private static void logWarning(String message) {
        if (Gdx.app != null) Gdx.app.log("OutOfTower", "WARNING: " + message);
        else System.err.println("[OutOfTower] WARNING: " + message);
    }
}
