# Workflows

In `.github/workflows/`. Actions are pinned by major version, the same ones as in `modpack`.

| Workflow | When it runs | What it does |
| --- | --- | --- |
| `validar.yaml` | push (except documentation-only changes) and PR | Validates the Gradle wrapper; checks `mod_version` and its `CHANGELOG.md` section; runs `./gradlew build` (compile, unit tests, contract test, jar); stores the jar. On PRs into `main`, the *Entrada na main* job checks the source branch, a stable version and a version higher than `main`'s |
| `integracao.yaml` | PRs into `main` that touch code, every Monday, on demand (with a pack branch or tag) | Builds and runs `scripts/integration-test.sh`: installs the pack with the modpack's `testar-servidor.sh`, starts the server and checks the bridge. Stores the log |
| `contrato.yaml` | every Monday, on demand | Tests the contract with the pack's version and the newest version of `fabric-permissions-api`; compares the pack with `tested-with.json`; opens an issue if something changed |
| `docs.yaml` | push and PR that change `.md` files | `lychee --offline` (links and anchors) and `markdownlint-cli2` |
| `regras-claude.yaml` | push and PR that change `CLAUDE.md` | Checks the IunaMC common-rules block |
| `release.yaml` | push of a `v*` tag | Checks the tag (format, version, annotated, verified, right branch), builds, tests, generates the SHA-256, attests provenance and creates the release with the text from `CHANGELOG.md` |

Secrets: none. `GITHUB_TOKEN` is enough; the attestation uses GitHub's own OIDC.
