package outoftower.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ContentRegistrationException extends IllegalStateException {
    private final List<String> errors;

    public ContentRegistrationException(List<String> errors) {
        super(format(errors));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public List<String> getErrors() {
        return errors;
    }

    private static String format(List<String> errors) {
        StringBuilder message = new StringBuilder("OutOfTower content registration failed:");
        for (String error : errors) message.append("\n - ").append(error);
        return message.toString();
    }
}
