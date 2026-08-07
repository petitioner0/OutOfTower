package outoftower;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PreStartGameSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import outoftower.map.CustomMap;
import outoftower.map.MapSaveManager;
import outoftower.map.PlayerPathTracker;
import outoftower.map.events.MyFirstEvent;
import outoftower.util.EventFactory;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class OutOfTower implements PreStartGameSubscriber, PostInitializeSubscriber {

    public static final String modID = "outoftower";

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    public static String makeImagePath(String resourcePath) {
        return modID + "Resources/images/" + resourcePath;
    }

    public OutOfTower() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new OutOfTower();
    }

    @Override
    public void receivePostInitialize() {
        BaseMod.addSaveField("OOT_PATH", new PlayerPathTracker());
        BaseMod.addSaveField("OutOfTower:MapData", new MapSaveManager());
        EventFactory.register(MyFirstEvent.ID, MyFirstEvent::new);
    }

    @Override
    public void receivePreStartGame() {
        // 继续存档时由 CustomSavable 恢复状态；真正的新一局必须先清掉
        // 上一局遗留的路径标记、玩家坐标和已释放的房间实例。
        if (!CardCrawlGame.loadingSave) {
            CustomMap.resetRunState();
        }
    }
}
