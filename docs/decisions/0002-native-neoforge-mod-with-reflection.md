# 0002: native NeoForge mod, Fabric API through reflection

- **Date:** 2026-10-29
- **Status:** accepted
- **Origin:** creation of this repository; evidence in
  [code findings](../reference/code-findings.md)

## Context

Two paths lead to the same bridge to LuckPerms:

1. a **Fabric** mod loaded by Connector, compiled with Loom against
   `fabric-permissions-api`, as EasyAuth does for LuckPerms;
2. a native **NeoForge** mod.

Confirmed in the code: a Fabric mod loaded by Connector can reach the LuckPerms
NeoForge API; Connector skips any jar that has a `neoforge.mods.toml`; the events
are a single shared object ([b](../reference/code-findings.md#b-how-connector-loads-these-jars)).

## Decision

A native NeoForge mod (`javafml`, `@Mod(dist = DEDICATED_SERVER)`), built with
Gradle and ModDevGradle. `fabric-permissions-api` is **not a compile
dependency**: the adapter (`FabricPermissionsApi`) reads it through reflection,
checks the shape of the four interfaces and registers a `java.lang.reflect.Proxy`
on each event. LuckPerms comes in as a `compileOnly` dependency on the 5.4 public
API and as an optional dependency.

Reasons:

- One toolchain (NeoForge) and one jar for Minecraft 1.21.1; updating it in the
  pack is the same as updating any NeoForge mod.
- No Loom, no Yarn/intermediary mappings, no dependency on the exact Fabric API
  version the mods embed (0.3.1 today, something else tomorrow).
- If the API disappears or changes, the mod keeps loading and says why: a shape
  mismatch becomes a warning in the log and the module stays idle, instead of a
  `NoClassDefFoundError`.
- The integration test proves it works: the pack's real server, with Banhammer's
  own code reading a LuckPerms node.

## Alternatives rejected

- **Fabric mod through Connector.** Requires Loom, depends on Connector's
  transformation (mappings, mixins) continuing to accept the jar, and couples our
  release cycle to Connector's. It brings no benefit: the event is already
  reachable from the NeoForge side.
- **Mixin into `Permissions.check`.** More invasive, and it would break with each
  version of the mods; the event is the API's intended extension point.
- **NeoForge `PermissionAPI` / `PermissionGatherEvent`.** LuckPerms already
  implements it, but the Fabric mods never query it.
- **Compiling against the Fabric API with our own stubs.** The types would have to
  be kept in sync by hand and would still fail to link at run time if they
  changed. Reflection, plus the contract test reading the real jar, gives the
  same warning without maintaining a copy.
- **Replacing the Fabric mods with NeoForge equivalents.** Out of scope; Banhammer
  and Styled Chat have no equivalent with the same behavior.

## Consequences

- Reflection has a maintenance cost: the expected shape lives in a single place
  (`FabricPermissionsApi`) and in a test that reads the published jar
  ([0006](0006-tests-and-breakage-detection.md)).
- The `Proxy` objects are created once, at startup; each query is a plain
  interface call (no reflection on the hot path).
