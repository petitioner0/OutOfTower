package outoftower;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PreStartGameSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import outoftower.map.MapSaveManager;
import outoftower.map.runtime.MapManager;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public final class OutOfTower implements PreStartGameSubscriber, PostInitializeSubscriber {
    public static final String modID = "outoftower";

    public OutOfTower() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new OutOfTower();
    }

    @Override
    public void receivePostInitialize() {
        BaseMod.addSaveField("OutOfTower:MapDataV1", new MapSaveManager());
    }

    @Override
    public void receivePreStartGame() {
        MapManager.prepareForGameStart(CardCrawlGame.loadingSave);
    }
}
