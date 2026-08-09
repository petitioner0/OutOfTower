# OutOfTower

OutOfTower is a Java 8 framework mod for building data-driven Slay the Spire maps. A content mod supplies JSON topology and normal `AbstractEvent` implementations; OutOfTower handles map rendering, navigation, deterministic event assignment, runtime edge changes, and persistence.

OutOfTower contains no default map. When no content pack is registered, every patch is a no-op and the game uses its vanilla map.

## Requirements and build

- Slay the Spire desktop
- ModTheSpire 3.18.2
- BaseMod 5.29.0
- Java 8-compatible source
- Maven 3

The build uses system-scoped jars from the Steam installation configured by `Steam.path` in `pom.xml`.

```sh
mvn test
mvn package
```

`mvn package` creates `target/OutOfTower.jar` and copies it to the configured Slay the Spire `mods` directory.

## Registering a content pack

Declare `outoftower` as a dependency in the content mod's `ModTheSpire.json`, then register exactly one content pack during BaseMod post-initialization:

```java
OutOfTowerApi.registerContent(
    ContentPack.builder("examplemod")
        .actMap(1, "examplemodResources/maps/act1.json")
        .event("examplemod:first", FirstEvent::new)
        .event("examplemod:second", SecondEvent::new)
        .build()
);
```

One content pack may provide a different JSON resource for each Act. If a registered content pack does not provide the current Act, map generation stops with a descriptive error. Only one content pack can be active at a time.

Map topology cannot be generated or changed through a Java builder. Java registration exists only because event ids need constructors.

## Map JSON v1

See [`examples/sample-content`](examples/sample-content) for a complete example.

```json
{
  "schemaVersion": 1,
  "mapId": "examplemod:act1",
  "mapVersion": 1,
  "size": { "width": 4, "height": 4 },
  "viewport": { "radius": 2 },
  "eventPools": {
    "wild": {
      "drawMode": "WITH_REPLACEMENT",
      "events": [
        { "eventId": "examplemod:first", "weight": 3 },
        { "eventId": "examplemod:second", "weight": 1 }
      ]
    }
  },
  "nodes": [
    {
      "id": "start",
      "x": 1,
      "y": 1,
      "type": "wild",
      "start": true,
      "eventPool": "wild",
      "links": ["east"]
    },
    {
      "id": "east",
      "x": 2,
      "y": 1,
      "type": "wild",
      "start": false,
      "eventPool": "wild",
      "links": [],
      "icon": {
        "image": "examplemodResources/images/map/wild.png",
        "outline": "examplemodResources/images/map/wild_outline.png"
      }
    }
  ]
}
```

Important rules:

- `mapId`, `eventId`, and state namespaces use `modid:name`; node and pool ids are local to one map.
- Coordinates are zero-based, unique, and must be inside `size`.
- Links are undirected. Declare an edge once, from either endpoint.
- `viewport.radius` is a Manhattan radius from 1 to 3.
- Every base or runtime edge must span no more than the viewport radius.
- `type` is a data label only; every node uses OutOfTower's event-room shell.
- Multiple `start` nodes are allowed and selected uniformly.
- Pools support `WITH_REPLACEMENT` and `WITHOUT_REPLACEMENT`. Weights are positive integers.
- Custom icons require both image and outline. Missing files fall back to the vanilla event icon with a warning.

Structural problems are aggregated into one registration error. Disconnected components are warnings because an event may connect them later.

## Runtime API

Events can query and mutate the active map without accessing framework internals:

```java
OutOfTowerApi.getCurrentMap().ifPresent(map -> {
    map.disconnect("start", "east");
    map.connect("start", "north");
    map.state().put("examplemod:quest", "gateOpen", new JsonPrimitive(true));
});
```

`MapRuntime` exposes node metadata, current node, visit counts, connectivity, `connect`, `disconnect`, and `MapStateStore`. Nodes cannot be created or removed at runtime. Edge operations are undirected and idempotent; invalid ids, self-links, and links longer than the viewport radius are rejected.

## Verification

`mvn test` covers schema validation, JSON order, deterministic original-RNG draws, event pool behavior, runtime edges, extension state, and save round trips. Changes to patches, rendering, or room transitions still require an in-game mouse/controller smoke test.
