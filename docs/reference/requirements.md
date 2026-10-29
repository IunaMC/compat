# Requirements

## Problem

LuckPerms for NeoForge answers NeoForge's `PermissionAPI` and replaces command
requirements with `command.*` nodes. It does not listen to the
`fabric-permissions-api` events ([finding](code-findings.md#c-native-neoforge-or-fabric-through-connector)).
The pack's Fabric mods, which run through Sinytra Connector, query only those
events. As a result, nodes such as `banhammer.duration.1d`,
`banhammer.ignoreTempLimit`, `styledchat.*` and `styledplayerlist.*` have no
effect, and only what LuckPerms injects as level 4 (`command.*`) works.

## Scope

| # | Requirement | Status |
| --- | --- | --- |
| R1 | Answer `PermissionCheckEvent` with the player's LuckPerms node, in the player's current contexts | done |
| R2 | Answer `OptionRequestEvent` with meta, prefix and suffix | done |
| R3 | Answer the offline events (`OfflinePermissionCheckEvent`, `OfflineOptionRequestEvent`) by UUID | done; the offline option query fails inside the API up to 0.3.3 (a bug in the API itself, fixed in 0.4.1) |
| R4 | Console, command block, RCON and a player with no loaded user: "no opinion" | done |
| R5 | Anything LuckPerms does not define keeps the calling mod's default | done |
| R6 | Register the known nodes with LuckPerms (tab completion, `/lp tree`, web editor) without granting anything | done |
| R7 | Structure in independent modules, for other pack compatibility fixes | done (`CompatModule`) |
| R8 | Minimal configuration to turn off the module and each of its parts | done |
| R9 | Diagnostics for the administrator: what the module does and what the API returns | done (`/iunacompat`) |

## Out of scope

- Granting or denying permissions: the mod only reads what LuckPerms already has.
- Replacing LuckPerms or building a permission system of its own.
- Changing the operator level of commands: that is LuckPerms' `command.*`.
- The client: the mod is server-only (`@Mod(dist = DEDICATED_SERVER)`).
- Fixing bugs in the queried mods (for example, the `styledchat.format..type.*`
  node with a double dot).

## Non-functional requirements

| Requirement | How it is met |
| --- | --- |
| Compatibility | Minecraft 1.21.1, NeoForge `[21.1.0,22)`, Java 21; LuckPerms 5.4 or newer (public API, compiled against 5.4) |
| Fail-safe | Nothing throws into a Fabric mod: the error is logged and counted, and the answer is "no opinion" ([0003](../decisions/0003-fail-safe-and-dependencies.md)) |
| Performance | An online-player query is a read from the LuckPerms cache (no allocation beyond LuckPerms' own, no I/O); an offline query reads from storage only if the user is not in memory, off the server thread when the caller is asynchronous |
| No side effects | With LuckPerms or the Fabric API missing, or with the API in a new shape, the module stays idle and the server behaves as if the mod were not installed |
| Observability | A startup log line per module, counters and `/iunacompat status`; `debug.logChecks` to log every query |
| Maintenance | Contract test against the published API, integration test against the pack, weekly check of the newest API and of the pack ([0006](../decisions/0006-tests-and-breakage-detection.md)) |
| Privacy | No data leaves the server; the repository is public and stores no hostnames, local paths or secrets |

## Configuration

File `config/iunacompat-common.toml`. The keys are listed in the [README](../../README.md#configuration).

## Logs

Minecraft's `slf4j` logger. `INFO` level when each module starts; `WARN` for
differences in the API shape; `ERROR` with the stack trace only on the first
backend failure (later ones become `DEBUG` and a counter, so the log is not flooded).
