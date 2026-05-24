package outoftower.map.nodes;


import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class AbstractMapNode {

    public final UUID id;
    public final int gx;
    public final int gy;

    private AbstractRoom cachedRoom = null;

    public boolean isStartCandidate = false;

    // 子类声明的“坐标邻接”
    protected final List<int[]> neighborCoords = new ArrayList<>();

    // 构建器最终填充的“类邻接”
    public final Set<Class<? extends AbstractMapNode>> neighbors = new HashSet<>();

    protected AbstractMapNode(int gx, int gy) {
        this.gx = gx;
        this.gy = gy;
        byte[] bytes = (gx + "," + gy).getBytes(StandardCharsets.UTF_8);
        this.id = UUID.nameUUIDFromBytes(bytes);
    }

    // 子类调用：声明邻居坐标
    protected void link(int nx, int ny) {
        neighborCoords.add(new int[]{nx, ny});
    }

    // 地图生成时调用
    public List<int[]> getCoordLinks() {
        return neighborCoords;
    }

    public AbstractRoom getRoom() {
        return cachedRoom;
    }

    /**
     * 地图 build 阶段调用：
     * 只创建 Room 实例，不触发任何 Event 行为
     */
    public final void initRoom() {
        if (cachedRoom == null) {
            cachedRoom = createRoom();
        }
    }

    /** 子类只负责“这个节点用什么 Room” */
    protected abstract AbstractRoom createRoom();
}