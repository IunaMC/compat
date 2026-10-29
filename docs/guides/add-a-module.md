# Add a module

Goal: add another compatibility fix to the mod without touching the existing ones.

## Steps

1. Create a subpackage `club.iuna.compat.<module>` with a class that implements
   `CompatModule` (`id`, `description`, `enable`, `status`). Rules:
   - `enable()` never throws: it returns a `ModuleStatus` with the state and a
     one-line explanation (`UNAVAILABLE` if the mod it adjusts is not installed).
   - Never load another mod's classes outside the module (so the mod keeps
     loading without it).
   - Give it its own section in `CompatConfig` (`<module>.enabled`, default `true`).
2. Register the module in `IunaCompat.MODULES`.
3. Write unit tests for its decision logic and, if the module reads another mod's
   code through reflection, a contract test like `FabricContractTest`.
4. Confirm the behavior in the adjusted mod's code (decompiled or open source)
   and record it in [code findings](../reference/code-findings.md) or in a new
   reference page.
5. Add checks to `scripts/integration-test.sh`.
6. Record the decision in `docs/decisions/` if an alternative was rejected.
7. Bump the MINOR part of `mod_version` and add a note to `CHANGELOG.md`.

## Verify

`./gradlew build` and `./scripts/integration-test.sh` pass, and the module shows
up in `/iunacompat status`.
