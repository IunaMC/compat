# Add to the pack

Goal: add (or update) a release jar in the `modpack` packwiz pack.

## Prerequisites

- The `vX.Y.Z` release exists in this repository ([publish](publish-a-version.md)).
- A green integration test with the current pack.
- Follow the `modpack` workflow (one commit per mod, `devel/devel`, MINOR pack version).

## Steps

1. Download the jar and the `.sha256` from the release and verify them:

   ```sh
   gh release download vX.Y.Z --repo IunaMC/compat --pattern 'iunacompat-*' --dir /tmp/iunacompat
   ```

   ```sh
   (cd /tmp/iunacompat && sha256sum -c *.sha256)
   ```

2. In `modpack`, create or update `pack/mods/iuna-compat.pw.toml`:

   ```toml
   name = "IUNA Compat"
   filename = "iunacompat-X.Y.Z-mc1.21.1.jar"
   side = "server"

   [download]
   url = "https://github.com/IunaMC/compat/releases/download/vX.Y.Z/iunacompat-X.Y.Z-mc1.21.1.jar"
   hash-format = "sha256"
   hash = "<sha256 of the jar>"
   ```

   No `[update.*]`: the mod is server-only and does not go to CurseForge
   ([decision 0004](../decisions/0004-distribution-via-github-release.md)).
3. Run `cd pack && packwiz refresh` and `python3 scripts/pack.py readme`.
4. Bump the pack version (MINOR), write the modpack's `MUDANCAS.md` entry and
   commit: `feat(mods): adiciona IUNA Compat X.Y.Z no servidor` (or `atualiza`).
5. Run the modpack's boot test (`/testar` or `scripts/testar-servidor.sh`).

## Verify

The modpack's Validar workflow passes and the server starts with
`[permissions] ENABLED` in the log. In production, `/iunacompat status` shows the
module and the consumer mods.

No behavior changes until LuckPerms defines a node for the Fabric mods: see the
[reference](../reference/code-findings.md#e-effects-of-enabling-the-bridge).
