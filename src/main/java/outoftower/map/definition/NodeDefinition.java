package outoftower.map.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodeDefinition {
    private final String id;
    private final int x;
    private final int y;
    private final String type;
    private final boolean start;
    private final String eventPool;
    private final List<String> links;
    private final IconDefinition icon;

    public NodeDefinition(String id, int x, int y, String type, boolean start,
                          String eventPool, List<String> links, IconDefinition icon) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.start = start;
        this.eventPool = eventPool;
        this.links = Collections.unmodifiableList(new ArrayList<>(links));
        this.icon = icon;
    }

    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getType() { return type; }
    public boolean isStart() { return start; }
    public String getEventPool() { return eventPool; }
    public List<String> getLinks() { return links; }
    public IconDefinition getIcon() { return icon; }
}
