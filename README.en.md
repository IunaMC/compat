# IUNA Compat

Server-side compatibility layer for the [IUNA pack](https://github.com/IunaMC/modpack)
(Minecraft 1.21.1, NeoForge 21.1.x). Each fix is an independent module.

[Português](README.md)

## Modules

| Module | What it fixes |
| --- | --- |
| `permissions` | Bridges `fabric-permissions-api` to LuckPerms. Fabric mods running through Sinytra Connector (Banhammer, Styled Chat, Styled Player List, Essential Commands, EasyAuth) check nodes such as `banhammer.duration.1d`, but LuckPerms for NeoForge only answers NeoForge's `PermissionAPI` and the `command.*` nodes. Without this mod those nodes are ignored. |

## Requirements

- Minecraft 1.21.1 and NeoForge 21.1.x.
- LuckPerms 5.4 or newer for the `permissions` module. Without it (or without any
  Fabric mod embedding `fabric-permissions-api`) the module stays idle and says so
  in the log; the server behaves as if the mod were not installed.
- Server side only.

## How it works

The mod registers listeners on the four `fabric-permissions-api` events
(`PermissionCheckEvent`, `OptionRequestEvent` and the two offline variants) and
answers through LuckPerms' public API, like LuckPerms' own Fabric listener. When
LuckPerms does not define a node the answer is "no opinion" and the queried mod
keeps its own default, so installing the mod changes nothing until a node is
set. Design notes (in Portuguese) are in [`docs/`](docs/README.md).

## Configuration

`config/iunacompat-common.toml`, created on first start.

| Key | Default | Effect |
| --- | --- | --- |
| `permissions.enabled` | `true` | Turns the module on or off |
| `permissions.options` | `true` | Also answers options (meta, prefix, suffix) |
| `permissions.offline` | `true` | Also answers queries about offline players |
| `permissions.registerKnownNodes` | `true` | Introduces known nodes to LuckPerms for tab completion and the web editor (grants nothing) |
| `debug.logChecks` | `false` | Logs every query (very noisy) |

## Diagnostics

From the console or with operator level 4:

```text
/iunacompat status
/iunacompat check <uuid> <node> [online]
/iunacompat option <uuid> <key> [online]
```

## Build

```sh
./gradlew build
```

Java 21. The jar is written to `build/libs/`.

## License

[MIT](LICENSE).
