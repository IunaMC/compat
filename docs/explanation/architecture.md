# Architecture and plan

## How the pieces fit together

```text
 Fabric mod (Banhammer, Styled Chat, ...)          NeoForge + LuckPerms
          │                                               ▲
          │ Permissions.check(source, node, default)      │ public API
          ▼                                               │
 fabric-permissions-api (a single copy, via Connector)    │
   PermissionCheckEvent.EVENT.invoker()                   │
          │                                               │
          ▼ Proxy registered through reflection           │
 FabricPermissionsApi ──► PermissionBridge ──► LuckPermsBackend
   (checks the shape)       (decides, counts,    (in-memory user,
                             never throws)         player contexts)
```

- `IunaCompat` is the entry point: it registers the config and the command and
  enables the modules on `FMLCommonSetupEvent`, when every mod (and the Fabric
  mods' classes) already exists.
- `CompatModule` is the unit of compatibility: `id`, `description`, `enable()`
  (never throws) and `status()`. The states are `ENABLED`, `DEGRADED`, `DISABLED`,
  `UNAVAILABLE` and `FAILED`.
- `PermissionsModule` is the first module. It decides whether there is anything
  to do (config, LuckPerms, Fabric API), builds the bridge and the backend, and
  registers the listeners.
- `FabricPermissionsApi` is the **only** place that knows the shape of the Fabric API.
- `PermissionBridge` and `PermissionBackend` know no Fabric types; only
  `LuckPermsBackend` knows LuckPerms, and it is loaded only if LuckPerms is present.

## Flow of a query

1. The mod calls `Permissions.check(source, "banhammer.duration.1d")`.
2. The API invokes the `EVENT`; the `Proxy` calls `PermissionBridge.check`.
3. The bridge resolves the source: a `ServerPlayer` entity becomes a player;
   anything else gets no opinion.
4. The backend takes the LuckPerms user already in memory, builds the query
   options from the player's current contexts and reads the permission cache
   (`checkPermission`).
5. `TRUE`/`FALSE`/`DEFAULT` comes back as the API's `TriState`. `DEFAULT` makes
   the mod use its own default.

## Execution plan

| Step | Result | Status |
| --- | --- | --- |
| 1 | Confirm in the code the API, Connector, LuckPerms and what each mod consumes | done ([findings](../reference/code-findings.md)) |
| 2 | Requirements, decisions and this plan | done |
| 3 | Gradle project, `permissions` module, config, command, tests | done |
| 4 | CI (validation, integration, contract, docs) and signed release | done |
| 5 | `IunaMC/compat` repository, first release and integration into the pack | see [publish](../guides/publish-a-version.md) and [add to the pack](../guides/add-to-the-pack.md) |
| 6 | Propose the LuckPerms nodes that start to take effect (private config repository) | proposal only, outside this repository |

## Maintenance chain

The bridge breaks for four reasons. Each one has an alarm.

| What changes | How it shows up | Alarm |
| --- | --- | --- |
| `fabric-permissions-api` (a new published version) | The adapter does not recognize the shape and leaves the module idle | The weekly *Contrato* workflow runs against `latest.release` and opens an issue |
| The API version the pack's mods embed | Same, or a different event object | `FabricContractTest` (pack version) and `/iunacompat status` (shows whether each mod uses the same event) |
| The pack (LuckPerms, Connector, Fabric mods, NeoForge) | Different behavior that the unit tests cannot see | The weekly `pack-drift.py` compares against `tested-with.json`; *Integração* starts the server |
| Minecraft or NeoForge (a new major version of the pack) | Does not compile (class names) or the mod does not load | `./gradlew build`; a new mod version with `mc<version>` in the name |

The cycle is described in the [update with the pack](../guides/update-with-the-pack.md) guide.
