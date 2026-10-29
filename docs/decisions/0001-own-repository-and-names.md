# 0001: own repository, name, identifier, package, license and modules

- **Date:** 2026-10-29
- **Status:** accepted
- **Origin:** creation of this repository

## Context

The pack's Fabric mods (Banhammer, Styled Chat, Styled Player List, Essential
Commands, EasyAuth) query `fabric-permissions-api`, and LuckPerms for NeoForge
does not answer it. A web search found no ready-made bridge. The pack already
ships third-party compatibility mods (`*-compat.pw.toml`); the goal is a layer of
our own that grows with the pack's needs.

## Decision

| Item | Choice |
| --- | --- |
| Repository | `IunaMC/compat`, public, with `main` and `devel/devel` like the other code repositories |
| Mod name | IUNA Compat |
| Identifier (`modId`) | `iunacompat` |
| Java package | `club.iuna.compat` (the community's `iuna.club` domain), one subpackage per module (`club.iuna.compat.permissions`) |
| License | MIT, same as `modpack` |
| Scope | General compatibility layer, server side only. The first module is `permissions`; every other fix is a new module, with no dependencies between modules |
| Versions | Minecraft 1.21.1, NeoForge `[21.1.0,22)`, Java 21 |

The generic name (`compat`) instead of `permissions-bridge` is deliberate: the
owner wants a general layer for the pack, and `permissions` is only the first
module. The cost is a less descriptive name; the README and `/iunacompat status`
say what each module does.

## Alternatives rejected

- **`iunamc.permissions-bridge` (one mod per fix).** Each future fix would become
  another repository, another jar and another pack entry, repeating the same CI
  and release chain.
- **`io.github.iunamc` package.** The project has its own domain, and the package
  follows it.
- **Apache-2.0 or GPL.** No reason to differ from the other IunaMC licenses.
- **Contributing a listener to LuckPerms itself.** LuckPerms for NeoForge does
  not load `fabric-permissions-api` by design: the API only exists in the pack
  because of Connector. It would be a long-term request outside our control. It
  can still be pursued in parallel, without depending on it.

## Consequences

- The pack now has a mod of its own, maintained here. The maintenance rules are
  in [update with the pack](../guides/update-with-the-pack.md).
- A new module is one class that implements `CompatModule` plus one line in
  `IunaCompat`; see [add a module](../guides/add-a-module.md).
