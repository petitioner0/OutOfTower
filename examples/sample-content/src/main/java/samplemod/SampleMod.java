package samplemod;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

@SpireInitializer
public final class SampleMod implements PostInitializeSubscriber {
    public SampleMod() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new SampleMod();
    }

    @Override
    public void receivePostInitialize() {
        SampleRegistration.register();
    }
}
