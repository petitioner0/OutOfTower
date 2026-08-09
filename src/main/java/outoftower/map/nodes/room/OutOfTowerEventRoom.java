package outoftower.map.nodes.room;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.map.runtime.ContentRegistry;

/**
 * The single event-room shell used by every data-driven map node.
 * Content mods implement AbstractEvent, not custom map node or room classes.
 */
public final class OutOfTowerEventRoom extends AbstractRoom {
    private final String nodeId;
    private final String plannedEventId;

    public OutOfTowerEventRoom(String nodeId, String plannedEventId) {
        if (nodeId == null || plannedEventId == null) {
            throw new IllegalArgumentException("nodeId and plannedEventId are required");
        }
        this.nodeId = nodeId;
        this.plannedEventId = plannedEventId;
        this.phase = RoomPhase.EVENT;
        this.waitTimer = 0.0F;
        this.mapSymbol = "?";
        this.mapImg = ImageMaster.MAP_NODE_EVENT;
        this.mapImgOutline = ImageMaster.MAP_NODE_EVENT_OUTLINE;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getPlannedEventId() {
        return plannedEventId;
    }

    @Override
    public void onPlayerEntry() {
        AbstractDungeon.overlayMenu.proceedButton.hide();
        if (event == null) {
            event = ContentRegistry.createEvent(plannedEventId);
            event.onEnterRoom();
        }
    }

    @Override
    public void update() {
        super.update();
        if (!AbstractDungeon.isScreenUp && event != null) event.update();
        if (event != null && event.waitTimer == 0.0F
                && !event.hasFocus && phase != RoomPhase.COMBAT) {
            phase = RoomPhase.COMPLETE;
            event.reopen();
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (event != null) {
            event.render(sb);
            // EventRoom cannot be used directly: AbstractDungeon replaces it with
            // a vanilla random room during nextRoomTransition().
            if (!(event instanceof AbstractImageEvent) || event.combatTime) {
                event.renderRoomEventPanel(sb);
                if (AbstractDungeon.screen != AbstractDungeon.CurrentScreen.VICTORY) {
                    AbstractDungeon.player.render(sb);
                }
            }
        }

        if (monsters != null && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.DEATH) {
            monsters.render(sb);
        }
        if (phase == RoomPhase.COMBAT) AbstractDungeon.player.renderPlayerBattleUi(sb);
        for (AbstractPotion potion : potions) {
            if (!potion.isObtained) potion.render(sb);
        }
        for (AbstractRelic relic : relics) relic.render(sb);
        renderTips(sb);
    }

    @Override
    public void renderAboveTopPanel(SpriteBatch sb) {
        super.renderAboveTopPanel(sb);
        if (event != null) event.renderAboveTopPanel(sb);
    }
}
