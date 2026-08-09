package outoftower.map.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EventPoolDefinition {
    private final String id;
    private final DrawMode drawMode;
    private final List<EventEntryDefinition> events;

    public EventPoolDefinition(String id, DrawMode drawMode, List<EventEntryDefinition> events) {
        this.id = id;
        this.drawMode = drawMode;
        this.events = Collections.unmodifiableList(new ArrayList<>(events));
    }

    public String getId() { return id; }
    public DrawMode getDrawMode() { return drawMode; }
    public List<EventEntryDefinition> getEvents() { return events; }
}
