package outoftower.map.events;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;

public class MyFirstEvent extends AbstractImageEvent {

    public static final String ID = "OutOfTower:MyFirstEvent";
    private static final EventStrings eventStrings =
            CardCrawlGame.languagePack.getEventString(ID);

    public static final String NAME = eventStrings.NAME;
    public static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    public static final String[] OPTIONS = eventStrings.OPTIONS;

    public MyFirstEvent() {
        super(
                NAME,
                DESCRIPTIONS[0],
                "outoftowerResources/images/events/test_event.png"
        );

        // 只有一个按钮
        imageEventText.setDialogOption(OPTIONS[0]);
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        // 点击后直接退出事件
        openMap();
    }
}