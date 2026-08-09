# Repository Guidelines

## Project Overview

This is a Java 8 Slay the Spire mod named `OutOfTower`. The Maven build produces `target/OutOfTower.jar` and, during `package`, copies it into the local Slay the Spire `mods` directory configured by `Steam.path` in `pom.xml`.

Core dependencies are local/system jars from a Steam install:

- Slay the Spire desktop jar
- ModTheSpire
- BaseMod
- StSLib

Builds can fail on machines that do not have these jars at the configured paths.

## Important Files

- `pom.xml`: Maven project metadata, Java 8 compiler settings, local Steam paths, resource filtering, and package-time jar copy.
- `src/main/java/outoftower/OutOfTower.java`: Mod entrypoint and BaseMod subscriptions. Registers save fields, events, strings, keywords, and audio.
- `src/main/java/outoftower/patches/`: ModTheSpire patches that alter map behavior, node rendering, clicks, start-node entry, and map positioning.
- `src/main/java/outoftower/map/`: Custom map state, graph construction, positioning, save/load, path tracking, and event rooms.
- `src/main/java/outoftower/util/NodeRegistry.java`: Registers map nodes and coordinate lookup. Current implementation registers an 11x11 grid of `WildEventNode`.
- `src/main/resources/ModTheSpire.json`: Filtered by Maven using project properties.
- `src/main/resources/outoftowerResources/`: Images and localization resources. Keep paths consistent with `OutOfTower.makePath` and `makeImagePath`.


## Important Reference

- `reference/` contain the important source code of dependence. Which store all the code with text format by none relation separate class.
- For any task involving source-code dependencies, you must first inspect the `reference/` directory to see whether the required source code has already been provided. 
Do not immediately rely on external assumptions, package documentation, or generated implementations. 
If the relevant source code is not found in `reference/`, inform the user which class source code was not found, then ask for the class needed for reference.


## Build And Verification

Use Maven from the repo root:

```sh
mvn test
mvn package
```

Notes:

- `mvn package` has the side effect of copying the jar to `${SlayTheSpire.mods}`.
- The project currently has no obvious dedicated automated test suite, so compile/package checks are the main quick verification path.
- If Maven cannot resolve system-scoped jars, inspect `Steam.path` and the installed workshop dependency paths in `pom.xml`.

## Coding Conventions

- Keep code Java 8 compatible.
- Follow the existing package layout under `outoftower`.
- Preserve the mod id `outoftower`; it must stay aligned between `pom.xml`, `OutOfTower.modID`, resource paths, and `ModTheSpire.json`.
- Existing comments are a mix of English and Chinese. It is fine to continue that style when clarifying nearby logic.
- Prefer small, local changes around the current systems rather than broad rewrites of map, save, or patch behavior.

## Custom Map Notes

- `CustomMap.init()` builds the graph, chooses/restores the player node, records the start visit, and recalculates positions.
- `StaticGraphMapBuilder.build()` creates rooms, attaches custom icons for `OutOfTowerEventRoom`, creates map edges, and rebuilds `AbstractDungeon.map`.
- `MapSaveManager` persists event ids and the current player node. Be careful when changing initialization order, because saved event restoration depends on rooms and map nodes being available.
- `NodeRegistry.coordMap` is the authoritative lookup for all registered node instances. `NodeRegistry.nodes` is keyed by node class and will collapse multiple instances of the same node class.
- Movement validity currently depends on `CustomMap.canReachFromPlayer()` and patch logic in `ClickPatch`.

## Resource Notes

- Localization files live under `outoftowerResources/localization/eng` and `outoftowerResources/localization/zhs`.
- `OutOfTower.SupportedLanguages` currently includes only `ENG`; unsupported languages fall back to `eng`.
- Maven filters `ModTheSpire.json` and JSON files under `${ModID}Resources`, so be cautious with literal `${...}` text in those resources.
