# IUNA Compat

Server-side compatibility layer for the [IUNA pack](https://github.com/IunaMC/modpack)
(Minecraft 1.21.1, NeoForge 21.1.x). Each fix is an independent module.

[Português](README.md)

## Modules

| Module | What it fixes |
| --- | --- |
| `permissions` | Bridges `fabric-permissions-api` to LuckPerms. Fabric mods running through Sinytra Connector (Banhammer, Styled Chat, Styled Player List, Essential Commands, EasyAuth) check nodes such as `banhammer.duration.1d`, but LuckPerms for NeoForge only answers NeoForge's `PermissionAPI` and the `command.*` nodes. Without this mod, those nodes are ignored. |

## Requirements

- Minecraft 1.21.1 and NeoForge 21.1.x.
- LuckPerms 5.4 or newer for the `permissions` module. Without it (or without any
  Fabric mod that embeds `fabric-permissions-api`), the module stays idle and
  says so in the log; the server behaves as if the mod were not installed.
- Server side only. Players install nothing.

## How it works

The mod registers listeners on the four `fabric-permissions-api` events
(`PermissionCheckEvent`, `OptionRequestEvent` and their two offline variants)
and answers through the LuckPerms public API, the same way the official
LuckPerms Fabric listener does. When LuckPerms does not define a node, the
answer is "no opinion" and the calling mod keeps its own default: installing the
mod changes nothing until someone sets a node. The reasoning behind each choice
is in [`docs/`](docs/README.md) (in Portuguese).

## Configuration

`config/iunacompat-common.toml`, created on first start.

| Key | Default | Effect |
| --- | --- | --- |
| `permissions.enabled` | `true` | Turns the module on or off |
| `permissions.options` | `true` | Also answers option queries (meta, prefix and suffix) |
| `permissions.offline` | `true` | Also answers queries about offline players |
| `permissions.registerKnownNodes` | `true` | Registers the known nodes with LuckPerms for tab completion and the web editor (grants nothing) |
| `debug.logChecks` | `false` | Logs every query (very verbose) |

## Diagnostics

From the console or with operator level 4:

```text
/iunacompat status
/iunacompat check <uuid> <node> [online]
/iunacompat option <uuid> <key> [online]
```

`check` and `option` query the Fabric API the same way a mod does, so the answer
is exactly what Banhammer, Styled Chat and the others receive.

## Build

```sh
./gradlew build
```

Requires Java 21. The jar is written to `build/libs/`. The tests and the
integration test are covered in the [guides](docs/README.md#guias) (in Portuguese).

## License

[MIT](LICENSE).
