package outoftower.map.util;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.map.CustomMap;
import outoftower.map.nodes.AbstractMapNode;

import java.util.UUID;

public class NodeEnterer {

    public static void enter(UUID id) {
        AbstractMapNode node = CustomMap.nodes.get(id);
        AbstractRoom room = node.getRoom();
    
        MapRoomNode fakeNode = new MapRoomNode(node.gx, node.gy);
        fakeNode.room = room;
        AbstractDungeon.currMapNode = fakeNode;
    
        AbstractDungeon.actionManager.clear();
    
        room.phase = AbstractRoom.RoomPhase.INCOMPLETE;
        room.onPlayerEntry();
    
        AbstractDungeon.scene.nextRoom(room);
    
        CustomMap.playerNode = id;
    }
}
