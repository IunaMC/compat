# Publish a version

Goal: take a change from `devel/devel` to a GitHub release.

## Prerequisites

- `git` with signing configured and the key registered on GitHub (the tag must
  show as *verified*).
- `gh` logged in.
- A green integration test for the pack version in use
  ([server test](server-test.md)).

## Steps

1. On `devel/devel`, bump `mod_version` in `gradle.properties` (SemVer) and write
   the `## X.Y.Z` section in `CHANGELOG.md`. Commit as
   `build: prepara a versão X.Y.Z`.
2. Optional, to test first: push a pre-release tag on `devel/devel`:

   ```sh
   git tag -s vX.Y.Z-beta.1 -m "vX.Y.Z-beta.1" && git push origin vX.Y.Z-beta.1
   ```

   (`mod_version` must be `X.Y.Z-beta.1` in that commit).
3. Open the PR from `devel/devel` into `main` and wait for Validar, Integração and
   *Entrada na main* to turn green:

   ```sh
   gh pr create --base main --head devel/devel
   ```

4. Merge with a merge commit, without deleting the branch:

   ```sh
   gh pr merge --merge
   ```

5. Update `devel/devel`:

   ```sh
   git fetch && git switch devel/devel && git merge --ff-only origin/main && git push
   ```

6. On `main`, create and push the signed tag:

   ```sh
   git switch main && git pull --ff-only
   git tag -s vX.Y.Z -m "vX.Y.Z" && git push origin vX.Y.Z
   ```

## Verify

The *Release* workflow finishes green and the `vX.Y.Z` release has the jar, the
`.sha256` and the attestation (`gh attestation verify <jar> --repo IunaMC/compat`).
Then follow [add to the pack](add-to-the-pack.md).

If the tag was wrong or the workflow rejected it, delete the remote tag
(`git push origin :refs/tags/vX.Y.Z`) and the local one, fix the problem and
create the tag again. Nothing is published until the workflow passes its checks.
