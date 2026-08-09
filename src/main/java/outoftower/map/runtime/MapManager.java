package outoftower.map.runtime;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.api.MapRuntime;
import outoftower.map.StaticGraphMapBuilder;
import outoftower.map.definition.MapDefinition;
import outoftower.map.save.MapSaveDataV1;

import java.util.Optional;

/** Owns the active per-Act session and the pending save handoff. */
public final class MapManager {
    private static MapSession session;
    private static MapSaveDataV1 pendingSave;

    private MapManager() {
    }

    public static Optional<MapRuntime> getRuntime() {
        return Optional.<MapRuntime>ofNullable(session);
    }

    public static MapSession getSession() {
        return session;
    }

    /** Called after vanilla generateMap. Returns true when a custom map replaced it. */
    public static boolean activateForCurrentAct() {
        if (!ContentRegistry.hasContent()) return false;
        int actIndex = currentActIndex();
        MapDefinition definition = ContentRegistry.requireMapForAct(actIndex);

        if (session == null || session.getActIndex() != actIndex
                || !session.getMapId().equals(definition.getMapId())) {
            if (pendingSave != null) {
                session = MapSession.restore(actIndex, definition, pendingSave);
                pendingSave = null;
            } else if (CardCrawlGame.loadingSave) {
                // BaseMod may deliver CustomSavable data after generateMap. Do not
                // consume mapRng while waiting for the saved assignments.
                StaticGraphMapBuilder.buildLoadingPlaceholder();
                return true;
            } else {
                session = MapSession.createNew(actIndex, definition, AbstractDungeon.mapRng);
            }
        }
        StaticGraphMapBuilder.build(session);
        return true;
    }

    public static void load(MapSaveDataV1 data) {
        if (data == null) {
            pendingSave = null;
            if (ContentRegistry.hasContent() && CardCrawlGame.loadingSave) {
                throw new IllegalStateException("This save has no compatible OutOfTower map data");
            }
            return;
        }
        pendingSave = data;
        if (!ContentRegistry.hasContent()) return;

        MapDefinition definition = ContentRegistry.requireMapForAct(data.actIndex);
        session = MapSession.restore(data.actIndex, definition, data);
        pendingSave = null;
        // When BaseMod restores fields after generateMap, replace the temporary
        // vanilla map now that no RNG-consuming initialization is required.
        if (AbstractDungeon.map != null) StaticGraphMapBuilder.build(session);
        restoreLoadedRoom();
    }

    public static MapSaveDataV1 save() {
        return session == null ? null : session.toSaveData();
    }

    public static void prepareForGameStart(boolean loadingSave) {
        session = null;
        if (!loadingSave) pendingSave = null;
        AbstractDungeon.nextRoom = null;
    }

    public static void clear() {
        session = null;
        pendingSave = null;
    }

    private static int currentActIndex() {
        return Math.max(1, AbstractDungeon.actNum);
    }

    /** BaseMod restores CustomSavable fields after the vanilla load transition. */
    private static void restoreLoadedRoom() {
        if (!CardCrawlGame.loadingSave || session == null) return;
        MapRoomNode currentNode = session.getNativeNodes().get(session.getCurrentNodeId());
        if (currentNode == null || currentNode.room == null) return;

        AbstractRoom previousRoom = AbstractDungeon.currMapNode == null
                ? null : AbstractDungeon.currMapNode.room;
        if (previousRoom != null && previousRoom != currentNode.room) previousRoom.dispose();

        AbstractDungeon.currMapNode = currentNode;
        AbstractDungeon.nextRoom = currentNode;
        AbstractDungeon.actionManager.clear();
        currentNode.room.phase = AbstractRoom.RoomPhase.INCOMPLETE;
        currentNode.room.onPlayerEntry();
        AbstractDungeon.scene.nextRoom(currentNode.room);
        AbstractDungeon.rs = currentNode.room.event instanceof AbstractImageEvent
                ? AbstractDungeon.RenderScene.EVENT
                : AbstractDungeon.RenderScene.NORMAL;
    }
}
