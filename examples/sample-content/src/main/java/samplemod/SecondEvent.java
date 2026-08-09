package samplemod;

import com.megacrit.cardcrawl.events.AbstractEvent;
import outoftower.api.OutOfTowerApi;

/** Demonstrates removing a base edge. */
public final class SecondEvent extends AbstractEvent {
    public SecondEvent() {
        roomEventText.updateBodyText("The eastern road collapses behind you.");
        roomEventText.addDialogOption("Continue");
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        OutOfTowerApi.getCurrentMap().ifPresent(map -> map.disconnect("start", "east"));
        roomEventText.clear();
        openMap();
    }
}
