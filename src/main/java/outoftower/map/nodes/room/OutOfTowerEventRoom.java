package outoftower.map.nodes.room;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.map.nodes.icon.IconType;
import outoftower.util.EventFactory;

public abstract class OutOfTowerEventRoom extends AbstractRoom {

    // 存档/恢复用：你也可以直接用 plannedEventId 作为保存字段
    public String savedEventId = null;

    // 地图阶段确定的事件ID（关键：地图阶段只决定它，不创建事件实例）
    protected String plannedEventId = null;

    protected IconType iconType;

    public OutOfTowerEventRoom(IconType iconType) {
        this.iconType = iconType;
        this.phase = RoomPhase.EVENT;
        this.waitTimer = 0.0F;
        this.mapSymbol = "?";
        this.mapImg = ImageMaster.MAP_NODE_EVENT;
        this.mapImgOutline = ImageMaster.MAP_NODE_EVENT_OUTLINE;
    }

    /** 子类只负责：从“指定事件池”里挑一个事件ID（测试阶段可直接返回固定ID） */
    protected abstract String rollEventId();

    /** 地图初始化时由 StaticGraphMapBuilder 调用：只决定 plannedEventId */
    public final void initEventIfNeeded() {
        // 1) 优先从存档恢复
        if (plannedEventId == null && savedEventId != null) {
            plannedEventId = savedEventId;
        }

        // 2) 正常抽取（但只抽ID，不创建 event 实例）
        if (plannedEventId == null) {
            plannedEventId = rollEventId();
        }

        // 3) 同步给存档字段（可选，但很实用）
        savedEventId = plannedEventId;
    }

    @Override
    public void onPlayerEntry() {
        AbstractDungeon.overlayMenu.proceedButton.hide();

        // 进入房间才真正实例化事件
        if (this.event == null) {
            // 极端保护：如果有人忘记在 build 阶段调用 initEventIfNeeded()
            if (plannedEventId == null) {
                plannedEventId = (savedEventId != null) ? savedEventId : rollEventId();
                savedEventId = plannedEventId;
            }

            AbstractEvent e = EventFactory.create(plannedEventId);
            this.event = e;
            if (this.event != null) {
                this.event.onEnterRoom(); // ★关键：对齐原版 EventRoom
                this.event.waitTimer = 0.1F;
            }
        }
    }

    @Override
    public void update() {
        super.update(); // ★ 让 AbstractRoom 驱动 updateDialog
        if (!AbstractDungeon.isScreenUp && event != null) {
            event.update(); // ★ 驱动事件逻辑
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (event != null) {
            event.render(sb);

            // 本类不能继承 EventRoom，否则 AbstractDungeon.nextRoomTransition
            // 会把预先安排好的事件替换成原版随机问号房。这里复刻 EventRoom
            // 调用 AbstractRoom.render 时的分支，避免被当作普通房间绘制角色。
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

        if (phase == RoomPhase.COMBAT) {
            AbstractDungeon.player.renderPlayerBattleUi(sb);
        }

        for (AbstractPotion potion : potions) {
            if (!potion.isObtained) {
                potion.render(sb);
            }
        }

        for (AbstractRelic relic : relics) {
            relic.render(sb);
        }

        renderTips(sb);
    }

    @Override
    public void renderAboveTopPanel(SpriteBatch sb) {
        super.renderAboveTopPanel(sb);
        if (event != null) {
            event.renderAboveTopPanel(sb);
        }
    }

    public IconType getIconType() {
        return iconType;
    }
}
