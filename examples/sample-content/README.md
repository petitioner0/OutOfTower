# Sample OutOfTower content pack

This directory is documentation and is not packaged into `OutOfTower.jar`.

This is a complete, separately buildable example. It contains a ModTheSpire initializer, two small `AbstractEvent` implementations, runtime edge changes, and an Act 1 JSON map.

The content mod's `ModTheSpire.json` must include:

```json
"dependencies": ["basemod", "outoftower"]
```

The example intentionally defines only Act 1. It must end the run before Act 2 or register another Act map, because a missing Act map is a configuration error once a content pack is active.

Build the root OutOfTower project first, then run `mvn package` in this directory. The example resolves `../../target/OutOfTower.jar` as its framework dependency.
