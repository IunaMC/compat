# 0006: tests and early breakage detection

- **Date:** 2026-10-29
- **Status:** accepted
- **Origin:** creation of this repository

## Context

The mod reads `fabric-permissions-api` through reflection and depends on the
behavior of LuckPerms, Connector and the pack's Fabric mods. A change in any of
them does not break compilation.

## Decision

Four layers, from cheapest to most complete:

1. **Unit tests** (`./gradlew test`, on every push): the bridge's decisions
   (`PermissionBridgeTest`), the mapping from `Tristate` to `Verdict` and from
   options to LuckPerms meta against a mocked API (`LuckPermsBackendTest`), and
   the adapter against copies of the API's shape and broken variants
   (`FabricPermissionsApiTest`).
2. **Contract test** (`FabricContractTest`, on every push): opens the published
   `fabric-permissions-api` jar (the version the pack embeds) with ASM and checks
   the exact shape the adapter relies on. The *Contrato* workflow runs the same
   test every week against the newest published version and opens an issue if it
   fails, before the pack updates to it.
3. **Integration test** (`scripts/integration-test.sh`, *Integração* workflow):
   starts the pack's real server with the jar and, using `lp` from the console,
   checks nodes and options read through the Fabric API (a simulated online
   player and an offline UUID) and through Banhammer's code. It runs on PRs into
   `main`, every week and on demand.
4. **Pack drift** (`scripts/pack-drift.py`, *Contrato* workflow): compares the
   versions of the pack, NeoForge and the watched mods against `tested-with.json`
   and opens an issue when they change. After a green integration test,
   `--update` records the new state.

## Alternatives rejected

- **Integration test only.** Slow (minutes and 1.5 GB), imprecise about what
  changed, and it does not run on every commit.
- **Compiling against the Fabric API.** A signature error would only show up if
  someone recompiled against the new version; see [0002](0002-native-neoforge-mod-with-reflection.md).
- **A real player in the integration test.** It requires a client or a full
  protocol implementation, including NeoForge's mod negotiation. NeoForge's fake
  player (`FakePlayerFactory`) goes through the same LuckPerms path (`getUser`,
  `ContextManager.getQueryOptions`) and the same mod code.

## Consequences

- What the test does not cover: the path of a truly connected player (network,
  login, real contexts). It is listed in the [guides](../guides/server-test.md#what-is-not-covered).
