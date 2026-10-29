# 0005: SemVer, branches and signed-tag releases

- **Date:** 2026-10-29
- **Status:** accepted
- **Origin:** creation of this repository

## Decision

- **Version** (`mod_version` in `gradle.properties`, SemVer): MAJOR changes
  behavior or requires another Minecraft or NeoForge version; MINOR is a new
  module or option; PATCH is a fix. Notes go in `CHANGELOG.md`, one `## X.Y.Z`
  section per version (the workflow uses it as the release text).
- **Jar name:** `iunacompat-<version>-mc<minecraft>.jar`, for example
  `iunacompat-0.1.0-mc1.21.1.jar`.
- **Branches:** same as the other code repositories: work happens on
  `devel/devel` (or on `devel/<task>` with a PR into it); `main` only accepts PRs
  from `devel/devel` or `docs/*`, merged with a merge commit
  (`gh pr merge --merge`), never rebase-merged; `devel/devel` is permanent.
- **Entrada na main** (a job in Validar): allowed source branch, stable version,
  and a version higher than `main`'s when the code changed.
- **Release:** push the tag `vX.Y.Z` (stable, from a `main` commit) or
  `vX.Y.Z-beta.N` (from a `devel/devel` commit). The workflow requires an
  **annotated tag verified by GitHub** (GPG or SSH signature of whoever created
  it), a version equal to the one in `gradle.properties`, no existing release and
  a commit on the right branch. It then builds, tests, generates the SHA-256,
  attests provenance (Sigstore, through the `actions/attest` action) and creates
  the release.

## Alternatives rejected

- **A release on every push to `main`, like the modpack.** The modpack versions
  through `pack.toml`; here the tag is the explicit act of publishing, and the
  signature stays with the person, with no signing key stored in CI.
- **Signing the jar with GPG in CI.** It would require storing a private key in a
  secret. GitHub's provenance attestation proves the jar came from this
  repository and this workflow without any secret.
- **Tag versions without beta.** `devel/devel` needs a testing path (`-beta.N`)
  before the stable release, as in the modpack.

## Consequences

- Whoever publishes needs their signing key registered on GitHub.
- A wrong tag publishes nothing (the workflow fails before creating the release);
  delete the remote tag and create it again.
