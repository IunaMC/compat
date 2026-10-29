# Build and test

Goal: build the mod and run the tests that do not need a server.

## Prerequisites

- Java 21 (`java -version`).
- Internet access the first time: ModDevGradle downloads Minecraft and NeoForge
  (about 800 MB, plus a few minutes to decompile and recompile Minecraft).

## Steps

1. Build and test:

   ```sh
   ./gradlew build
   ```

2. The jar is at `build/libs/iunacompat-<version>-mc1.21.1.jar`.
3. To run only the tests, or a single test:

   ```sh
   ./gradlew test
   ```

   ```sh
   ./gradlew test --tests '*PermissionBridgeTest*'
   ```

## Contract test

`FabricContractTest` opens the published `fabric-permissions-api` jar and checks
the shape of the events. By default it checks only the version the pack embeds
(`contract_versions` in `gradle.properties`). To check other versions, including
the newest:

```sh
./gradlew test --tests '*FabricContractTest*' -PcontractVersions=0.3.1,latest.release
```

If it fails with the new version, the adapter
(`permissions/fabric/FabricPermissionsApi`) must be adjusted before the pack
updates to it. See [update with the pack](update-with-the-pack.md).

## Verify

`BUILD SUCCESSFUL` and every test `PASSED`. The report is at
`build/reports/tests/test/index.html`.
