# OutOfTower

OutOfTower is a Java 8 mod for Slay the Spire. Its current playable core replaces the standard dungeon map with an 11×11 custom grid, renders custom node icons and reachable paths, and opens a minimal test event from each node.

## Requirements

- Slay the Spire desktop installation
- ModTheSpire 3.18.2
- BaseMod 5.29.0
- Java 8-compatible JDK
- Maven 3

The build uses system-scoped jars from the local Steam installation. Update `Steam.path` in `pom.xml` if Steam is installed elsewhere.

## Build

From the repository root:

```sh
mvn test
mvn package
```

`mvn package` creates `target/OutOfTower.jar` and copies it to the Slay the Spire `mods` directory configured by `SlayTheSpire.mods` in `pom.xml`.

The project currently has no dedicated automated test suite, so compilation plus an in-game smoke test is required after map, rendering, event, or save changes.
