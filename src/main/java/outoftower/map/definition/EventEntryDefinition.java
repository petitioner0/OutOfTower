package outoftower.map.definition;

public final class EventEntryDefinition {
    private final String eventId;
    private final int weight;

    public EventEntryDefinition(String eventId, int weight) {
        this.eventId = eventId;
        this.weight = weight;
    }

    public String getEventId() { return eventId; }
    public int getWeight() { return weight; }
}
