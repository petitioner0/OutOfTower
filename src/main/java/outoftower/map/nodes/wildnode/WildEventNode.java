package outoftower.map.nodes.wildnode;


import com.megacrit.cardcrawl.rooms.AbstractRoom;
import outoftower.map.nodes.AbstractMapNode;
import outoftower.map.nodes.room.wild.WildEventRoom;

// Node 系列都继承 AbstractMapNode
public class WildEventNode extends AbstractMapNode {

    public WildEventNode(int gx, int gy) {
        super(gx, gy);
        // 这里声明邻接坐标（仅提供坐标，不负责链接 MapRoomNode）
        link(gx + 1, gy);  // 示例：右边一个格子
        link(gx, gy + 1);  // 示例：上方一个格子
        link(gx -1, gy);
        link(gx, gy - 1);
        this.isStartCandidate = true;
    }

    @Override
    protected AbstractRoom createRoom() {
        // 房间由这个方法提供
        return new WildEventRoom();
    }
}