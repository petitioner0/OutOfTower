package samplemod;

import com.google.gson.JsonPrimitive;
import com.megacrit.cardcrawl.events.AbstractEvent;
import outoftower.api.OutOfTowerApi;

/** Demonstrates opening a shortcut and storing persistent quest state. */
public final class FirstEvent extends AbstractEvent {
    public FirstEvent() {
        body = "A hidden path appears between the start and north nodes.";
        hasDialog = true;
        roomEventText.addDialogOption("Open the shortcut");
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        OutOfTowerApi.getCurrentMap().ifPresent(map -> {
            map.connect("start", "north");
            map.state().put("samplemod:quest", "shortcutOpened", new JsonPrimitive(true));
        });
        roomEventText.clear();
        openMap();
    }
}
