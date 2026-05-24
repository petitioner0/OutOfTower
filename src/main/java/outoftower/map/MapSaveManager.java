package outoftower.map;

import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.map.nodes.AbstractMapNode;
import outoftower.map.nodes.room.OutOfTowerEventRoom;
import outoftower.util.SavedMapData;

import java.util.UUID;

public class MapSaveManager implements CustomSavable<SavedMapData> {

    @Override
    public SavedMapData onSave() {
        SavedMapData data = new SavedMapData();

        for (UUID id : CustomMap.nodes.keySet()) {
            AbstractMapNode node = CustomMap.nodes.get(id);
            AbstractRoom room = node.getRoom();

            if (room instanceof OutOfTowerEventRoom) {
                OutOfTowerEventRoom er = (OutOfTowerEventRoom) room;

                if (er.event != null) {
                    data.eventIds.put(id, er.event.getClass().getName());
                }
            }
        }

        data.playerNode = CustomMap.playerNode;

        return data;
    }

    @Override
    public void onLoad(SavedMapData data) {
        if (data == null) return;

        for (UUID id : data.eventIds.keySet()) {
            AbstractMapNode node = CustomMap.nodes.get(id);
            if (node == null) continue;
            
            AbstractRoom room = node.getRoom();

            if (room instanceof OutOfTowerEventRoom) {
                OutOfTowerEventRoom er = (OutOfTowerEventRoom) room;
                er.savedEventId = data.eventIds.get(id);  // ⭐ 设置房间要恢复事件
            }
        }

        if (data.playerNode != null) {
            CustomMap.playerNode = data.playerNode;
        
            if (!CustomMap.nodes.isEmpty()) {
                MapPositioner.recalc();
            }
        }
        
        // ⭐ 如果玩家当前所处房间是事件房间 → 要重新打开事件 UI
        AbstractRoom currentRoom = CustomMap.getCurrentRoom();
        if (currentRoom instanceof OutOfTowerEventRoom) {
        
            OutOfTowerEventRoom er = (OutOfTowerEventRoom) currentRoom;
        
            // 💡 这里必须让事件进入“房间逻辑”状态，否则 UI 不会显示
            er.onPlayerEntry();
        }
    }
}