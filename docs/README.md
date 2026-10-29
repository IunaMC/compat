# Documentation

Documentation for the `IUNA Compat` mod. It follows the writing standard of
[IunaMC/docs](https://github.com/IunaMC/docs/blob/main/referencia/padrao-de-escrita.md).
By owner decision, the code, comments, logs and documentation of this repository
are in English.

## Guides

| Guide | Purpose |
| --- | --- |
| [Build and test](guides/build-and-test.md) | Build the mod and run the unit and contract tests |
| [Server test](guides/server-test.md) | Start the pack's server with the mod and check the bridge end to end |
| [Publish a version](guides/publish-a-version.md) | Take a change from `devel/devel` to a GitHub release |
| [Add to the pack](guides/add-to-the-pack.md) | Add the release jar to the packwiz pack |
| [Update with the pack](guides/update-with-the-pack.md) | What to do when the pack, Minecraft or NeoForge changes |
| [Add a module](guides/add-a-module.md) | Add another compatibility fix |

## Reference

| Page | Contents |
| --- | --- |
| [Requirements](reference/requirements.md) | What the mod must do and what it does not do |
| [Code findings](reference/code-findings.md) | What was read in the jars and the open-source projects, with the class and the excerpt |
| [Workflows](reference/workflows.md) | What each workflow does and when it runs |

## Explanation

| Page | Contents |
| --- | --- |
| [Architecture and plan](explanation/architecture.md) | How the pieces fit together, the execution plan and the maintenance chain |

## Decisions

| Decision | Subject |
| --- | --- |
| [0001](decisions/0001-own-repository-and-names.md) | Own repository, name, identifier, package, license and modules |
| [0002](decisions/0002-native-neoforge-mod-with-reflection.md) | Native NeoForge mod, Fabric API through reflection |
| [0003](decisions/0003-fail-safe-and-dependencies.md) | Optional LuckPerms and failure without side effects |
| [0004](decisions/0004-distribution-via-github-release.md) | Distribution through the GitHub release, no CurseForge or Modrinth |
| [0005](decisions/0005-versioning-and-release.md) | SemVer, branches and signed-tag releases |
| [0006](decisions/0006-tests-and-breakage-detection.md) | Tests and early breakage detection |
