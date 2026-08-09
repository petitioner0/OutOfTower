package outoftower.api;

import java.util.Collection;

/** Runtime operations available to events in the active OutOfTower map. */
public interface MapRuntime {
    String getMapId();
    int getActIndex();
    String getCurrentNodeId();
    MapNodeView getNode(String nodeId);
    Collection<MapNodeView> getNodes();
    int getVisitCount(String nodeId);
    boolean hasVisited(String nodeId);
    boolean isConnected(String firstNodeId, String secondNodeId);
    boolean connect(String firstNodeId, String secondNodeId);
    boolean disconnect(String firstNodeId, String secondNodeId);
    MapStateStore state();
}
