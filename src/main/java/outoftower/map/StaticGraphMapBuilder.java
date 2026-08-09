package outoftower.map;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.EmptyRoom;
import outoftower.map.definition.NodeDefinition;
import outoftower.map.nodes.room.OutOfTowerEventRoom;
import outoftower.map.runtime.MapIconCache;
import outoftower.map.runtime.MapSession;
import outoftower.patches.NodeIconFields;

import java.util.ArrayList;

/** Materializes a logical MapSession into vanilla MapRoomNode objects. */
public final class StaticGraphMapBuilder {
    private static final int VANILLA_ROW_COUNT = 15;
    private static final int VANILLA_COLUMN_COUNT = 7;

    private StaticGraphMapBuilder() {
    }

    public static void build(MapSession session) {
        session.clearPresentation();

        for (NodeDefinition node : session.getDefinition().getNodes()) {
            OutOfTowerEventRoom room = new OutOfTowerEventRoom(
                    node.getId(), session.getAssignedEvent(node.getId()));
            MapRoomNode nativeNode = new MapRoomNode(node.getX(), node.getY());
            nativeNode.room = room;

            MapIconCache.TexturePair icon = MapIconCache.load(node.getIcon());
            if (icon.custom) {
                NodeIconFields.customIcon.set(nativeNode, icon.image);
                NodeIconFields.customOutline.set(nativeNode, icon.outline);
            }

            session.getRooms().put(node.getId(), room);
            session.getNativeNodes().put(node.getId(), nativeNode);
        }

        MapPositioner.invalidateEdgeLayout();
        MapPositioner.recalc();

        AbstractDungeon.map = new ArrayList<>();
        for (int row = 0; row < VANILLA_ROW_COUNT; row++) {
            ArrayList<MapRoomNode> columns = new ArrayList<>();
            for (int column = 0; column < VANILLA_COLUMN_COUNT; column++) {
                columns.add(new MapRoomNode(column, row));
            }
            AbstractDungeon.map.add(columns);
        }

        // Vanilla save loading indexes map[room_y][room_x]. The active custom
        // node is always centered at (3,7), so visible nodes must occupy their
        // current grid slots while hidden nodes can be appended outside the
        // seven vanilla index columns. Every real node remains in the outer map
        // exactly once and can move visually on later viewport recalculations.
        for (MapRoomNode nativeNode : session.getNativeNodes().values()) {
            if (nativeNode.x >= 0 && nativeNode.x < VANILLA_COLUMN_COUNT
                    && nativeNode.y >= 0 && nativeNode.y < VANILLA_ROW_COUNT) {
                AbstractDungeon.map.get(nativeNode.y).set(nativeNode.x, nativeNode);
            } else {
                AbstractDungeon.map.get(0).add(nativeNode);
            }
        }

        if (AbstractDungeon.dungeonMapScreen != null) {
            AbstractDungeon.dungeonMapScreen.updateImage();
        }
    }

    /**
     * Safe map used while BaseMod has not yet delivered CustomSavable data.
     * It prevents vanilla event/combat rooms from being entered during load and
     * does not require an RNG draw or any guessed custom-map state.
     */
    public static void buildLoadingPlaceholder() {
        AbstractDungeon.map = new ArrayList<>();
        for (int row = 0; row < VANILLA_ROW_COUNT; row++) {
            ArrayList<MapRoomNode> columns = new ArrayList<>();
            for (int column = 0; column < VANILLA_COLUMN_COUNT; column++) {
                MapRoomNode node = new MapRoomNode(column, row);
                node.room = new EmptyRoom();
                columns.add(node);
            }
            AbstractDungeon.map.add(columns);
        }
        if (AbstractDungeon.dungeonMapScreen != null) {
            AbstractDungeon.dungeonMapScreen.updateImage();
        }
    }
}
