# Update with the pack

Goal: keep the bridge working when the pack, Minecraft or NeoForge changes.

## When to use

- An automatic issue reported that the pack changed (*pack: versions changed...*)
  or that `fabric-permissions-api` changed shape (*contract: ...*).
- The pack is about to update LuckPerms, Connector, NeoForge or one of the
  watched Fabric mods (Banhammer, Styled Chat, Styled Player List, Essential
  Commands, EasyAuth).

## Steps

1. See what changed:

   ```sh
   python3 scripts/pack-drift.py
   ```

2. Run the integration test against the new pack (its branch or tag):

   ```sh
   ./scripts/integration-test.sh --modpack-ref <ref>
   ```

3. If it passed, record the new state and commit it as
   `build: registra o pack X.Y.Z como testado`:

   ```sh
   python3 scripts/pack-drift.py --update
   ```

4. If it failed, find the cause:
   - `/iunacompat status` shows `problem:` or `FAILED`: the API shape changed.
     Adjust `FabricPermissionsApi` and `FabricContractTest`, then run
     `./gradlew test --tests '*FabricContractTest*' -PcontractVersions=<version>`.
   - `consumer ...: uses ANOTHER copy of the event`: a mod started loading its
     own copy of the API. Turn on `debug.logChecks`, check how Connector resolved
     the jars (`logs/debug.log`, `Found valid mod file ... fabric_permissions_api_v0`)
     and adjust the adapter.
   - A LuckPerms error: check whether the public API changed (`LuckPermsBackendTest`).
5. New Minecraft or NeoForge version: bump `neo_version` and the version ranges in
   `gradle.properties`, build (`./gradlew build`), fix whatever breaks, run the
   integration test and publish a new version with the Minecraft version in the
   jar name ([publish](publish-a-version.md)). A change that requires another
   Minecraft version is MAJOR.
6. New mod version: [add it to the pack](add-to-the-pack.md).

## Verify

`python3 scripts/pack-drift.py` reports that the pack matches the tested state,
*Integração* passes, and `/iunacompat status` shows every consumer as `bridged`.
