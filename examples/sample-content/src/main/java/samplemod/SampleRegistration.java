package samplemod;

import outoftower.api.ContentPack;
import outoftower.api.OutOfTowerApi;

/** Call register() once from the content mod's receivePostInitialize(). */
public final class SampleRegistration {
    private SampleRegistration() {
    }

    public static void register() {
        OutOfTowerApi.registerContent(
                ContentPack.builder("samplemod")
                        .actMap(1, "samplemodResources/maps/act1.json")
                        .event("samplemod:first", FirstEvent::new)
                        .event("samplemod:second", SecondEvent::new)
                        .build()
        );
    }
}
