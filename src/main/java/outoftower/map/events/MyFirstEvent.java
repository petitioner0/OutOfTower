package outoftower.map.events;

import com.megacrit.cardcrawl.events.AbstractImageEvent;

import static outoftower.OutOfTower.makeID;
import static outoftower.OutOfTower.makeImagePath;

public class MyFirstEvent extends AbstractImageEvent {

    public static final String ID = makeID("MyFirstEvent");

    private static final String NAME = "My First Event";
    private static final String DESCRIPTION = "This is my very first event.";
    private static final String LEAVE_OPTION = "Leave";

    public MyFirstEvent() {
        super(
                NAME,
                DESCRIPTION,
                makeImagePath("events/test_event.png")
        );

        // 只有一个按钮
        imageEventText.setDialogOption(LEAVE_OPTION);
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        // 点击后直接退出事件
        openMap();
    }
}
