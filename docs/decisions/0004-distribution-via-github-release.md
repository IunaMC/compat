# 0004: distribution through the GitHub release, no CurseForge or Modrinth

- **Date:** 2026-10-29
- **Status:** accepted
- **Origin:** creation of this repository; context in
  [modpack decision 0008](https://github.com/IunaMC/modpack/blob/main/docs/decisoes/0008-sem-distribuicao-no-modrinth.md)

## Context

The pack is distributed only through CurseForge, and Modrinth is only a source of
mods (modpack decision 0008). Every `.pw.toml` that is not `server` has
`[update.modrinth]` and `[update.curseforge]` for the same file. A server-only
mod (`side = "server"`) stays out of the CurseForge `.zip` and is installed by
packwiz-installer when the server boots. The pack already has a path for jars
that do not come from the stores: [external files](https://github.com/IunaMC/modpack/blob/main/docs/guias/arquivos-externos.md),
with the jar attached to the modpack's own `arquivos-servidor` release.

## Decision

- The jar is published **only on this repository's GitHub release**, with the
  SHA-256 and the provenance attestation.
- The pack gets `pack/mods/iuna-compat.pw.toml` with `side = "server"`, a
  `[download]` section pointing to the release asset URL (`url`,
  `hash-format = "sha256"`, `hash`) and **no** `[update.*]`. There is no
  `[update.curseforge]` because the mod is not a client mod and CurseForge does
  not receive it.
- Each mod version is a new file with the version in its name; an asset is never
  replaced (the hash would change, and older pack versions pointing to the same
  URL would stop installing), following the modpack's external-files rule.
- Updating the mod in the pack is manual: change `url`, `filename` and `hash` in
  the `.pw.toml` (there is a guide), because `packwiz update` only follows Modrinth.

## Alternatives rejected

- **Publishing on Modrinth or CurseForge.** It requires a project page,
  moderation and a description for a mod that only serves this pack, and
  CurseForge would treat it as a client mod if it appeared in the `.zip`.
  Modpack decision 0008 removes Modrinth from distribution.
- **`pack.py externo` (attaching the jar to the modpack release).** It works,
  but it duplicates the jar and separates the mod's release from its repository.
  Here the release is the single source; the modpack only points to it. If the
  modpack ever wants to be self-contained, the jar can be attached there as well
  without changing anything in this repository.
- **Linking to the CI artifact.** It expires and requires a login.

## Consequences

- The pack now depends on `github.com/IunaMC/compat/releases` being up, as it
  already depends on the Modrinth CDN and on GitHub for the `.mrpack`.
- Changing the mod in the pack is a MINOR pack version (updated mod), with notes
  in the modpack's `MUDANCAS.md`.
- The repository must stay public: the server downloads without credentials.
