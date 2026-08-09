package outoftower.map.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MapLoadResult {
    private final MapDefinition definition;
    private final List<String> warnings;

    public MapLoadResult(MapDefinition definition, List<String> warnings) {
        this.definition = definition;
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    public MapDefinition getDefinition() { return definition; }
    public List<String> getWarnings() { return warnings; }
}
