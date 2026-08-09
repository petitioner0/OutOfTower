package outoftower.map.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MapValidationException extends IllegalArgumentException {
    private final List<String> errors;

    public MapValidationException(List<String> errors) {
        super(format(errors));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public List<String> getErrors() {
        return errors;
    }

    private static String format(List<String> errors) {
        StringBuilder message = new StringBuilder("Invalid OutOfTower map:");
        for (String error : errors) message.append("\n - ").append(error);
        return message.toString();
    }
}
