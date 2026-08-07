package outoftower.map.nodes.room.wild;

import outoftower.map.events.MyFirstEvent;
import outoftower.map.nodes.icon.IconType;
import outoftower.map.nodes.room.OutOfTowerEventRoom;


public class WildEventRoom extends OutOfTowerEventRoom {

    public WildEventRoom() {
        super(IconType.EVENT); // ⭐ 必须指定图标类型
    }

    @Override
    protected String rollEventId() {
        return MyFirstEvent.ID;
    }
}
