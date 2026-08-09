package outoftower.api;

/** Read-only node metadata exposed to event implementations. */
public final class MapNodeView {
    private final String id;
    private final int x;
    private final int y;
    private final String type;
    private final String eventPool;
    private final String assignedEventId;

    public MapNodeView(String id, int x, int y, String type, String eventPool, String assignedEventId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.eventPool = eventPool;
        this.assignedEventId = assignedEventId;
    }

    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getType() { return type; }
    public String getEventPool() { return eventPool; }
    public String getAssignedEventId() { return assignedEventId; }
}
