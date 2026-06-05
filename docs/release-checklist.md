# Release Checklist

Use this checklist before publishing or handing off a Sihriya build.

## Versioning

- Update `mod_version` in `gradle.properties`
- Update `CHANGELOG.md`
- Confirm `mods.toml` still expands the expected version metadata

## Functional verification

- Run `.\gradlew.bat build`
- Run `.\gradlew.bat validateClientAnimationPackaging`
- Run `.\gradlew.bat validateClientSmokeLog`
- Run `.\gradlew.bat validateVerificationServerLog`
- Run `.\gradlew.bat validateReleaseArtifacts`
- Run `.\gradlew.bat validateStandaloneReleaseServerSmoke`
- Run `.\gradlew.bat validateStandaloneReleaseClientSmoke`
- Run `.\gradlew.bat runClient -PforgeRunDir=run-client-clean --no-daemon`
- Run `.\gradlew.bat runServer -PforgeRunDir=run-clean --no-daemon`
- Confirm `run-client-clean/logs/latest.log` contains:
  - `Setting user: Dev`
  - `[statmod/]: STAT Mod loaded!`
  - `[sihriya/]: Sihriya chargé avec Epic Fight + STAT Mod !`
- Confirm `run-clean/logs/latest.log` contains:
  - `DedicatedServer/]: Done`
  - `[statmod/]: STAT Mod ready on server`
- Confirm `build/standalone-client/logs/latest.log` contains:
  - `Setting user: Dev`
  - `[statmod/]: STAT Mod loaded!`
  - `[sihriya/]: Sihriya chargé avec Epic Fight + STAT Mod !`
  - `OpenAL initialized`

## Dependency verification

- Confirm mandatory runtime contract is unchanged:
  - `forge`
  - `minecraft`
  - `epicfight`
  - `statmod`
- Confirm optional compat addons are not forced into default runtime

## Artifact verification

- Confirm output jar exists under `build/libs`
- Confirm `build/release-smoke/mods` contains both staged reobfuscated jars
- Confirm `validateStandaloneReleaseServerSmoke` passes
- Confirm `validateStandaloneReleaseClientSmoke` passes
- Confirm local mandatory Epic Fight runtime jars remain available in `libs/` or `../STAT_MOD/libs` for standalone smoke validation
- Confirm the local ForgeGradle asset cache is available, or pass an explicit assets path for standalone client smoke validation
- Confirm `README.md`, `CHANGELOG.md`, and `LICENSE` are current
- Confirm `docs/client-smoke-warning-baseline.md` still matches the current tolerated client warning baseline
- Confirm `docs/verification-warning-baseline.md` still matches the current tolerated warning baseline

## Handoff notes

- Record the exact STAT Mod sibling revision used for validation
- Record whether validation used the clean client smoke run `run-client-clean`
- Record whether validation used the clean verification run `run-clean`
- Do not publish while relying on an unexplained new warning in `run-client-clean/logs/latest.log`
- Do not publish while relying on an unexplained new warning in `run-clean/logs/latest.log`
