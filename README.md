# Sihriya

Sihriya is a Forge magic addon for Minecraft 1.20.1 built around Epic Fight and the sibling STAT Mod project. It adds school-based spell progression, mana, casting rules, Epic Fight-linked animations, and stat-driven spell scaling.

## Runtime contract

- Minecraft `1.20.1`
- Forge `47.4.20`
- Java `17+` for gameplay, JDK `21` accepted for local Gradle builds
- Mandatory mods:
  - `epicfight`
  - `statmod`
- Optional runtime compatibility:
  - Iron's Spells 'n Spellbooks

## Content snapshot

- 9 schools: `fire`, `water`, `wind`, `earth`, `lightning`, `ice`, `lava`, `necromancy`, `lumamancy`
- 252 spells total
- 28 spells per school
- per-school progression
- mana capability and sync
- spell cooldowns, temporary-effect rules, and server-side validation
- Epic Fight animation mappings and built-in data fixes

## Repository layout

Sihriya is built as a sibling of STAT Mod:

```text
STAT_MOD/
├── STAT_MOD/
└── SIHRIYA/
```

The `SIHRIYA` build expects the STAT Mod project at `../STAT_MOD`.

## Build

```powershell
.\gradlew.bat build
```

Artifacts are written to `build/libs`.

## Verification

Core local checks:

```powershell
.\gradlew.bat test
.\gradlew.bat validateSihriyaContent
.\gradlew.bat validateSihriyaPackaging
.\gradlew.bat validateClientAnimationPackaging
.\\gradlew.bat validateClientSmokeLog
.\gradlew.bat validateVerificationServerLog
.\\gradlew.bat validateReleaseArtifacts
.\\gradlew.bat validateStandaloneReleaseServerSmoke
.\\gradlew.bat validateStandaloneReleaseClientSmoke
```

Dedicated-server verification boot:

```powershell
.\gradlew.bat runServer -PforgeRunDir=run-clean --no-daemon
```

Clean client smoke boot:

```powershell
.\gradlew.bat runClient -PforgeRunDir=run-client-clean --no-daemon
```

Release-like smoke bundle staging:

```powershell
.\gradlew.bat prepareReleaseSmokeBundle
```

Standalone release-server smoke:

```powershell
.\gradlew.bat validateStandaloneReleaseServerSmoke
```

Standalone release-client smoke:

```powershell
.\gradlew.bat validateStandaloneReleaseClientSmoke
.\gradlew.bat runGameTestServer -PforgeRunDir=run-gametest --no-daemon
```

The verification run uses an isolated `run-clean` directory with:
- accepted EULA
- dedicated verification port `25566`
- stable default server configs
- log validation against known local regressions

The client smoke run uses an isolated `run-client-clean` directory with:
- stable client/common config templates
- log validation against known local regressions
- animation packaging validation for Epic Fight-scanned assets

`prepareReleaseSmokeBundle` stages the reobfuscated Sihriya and STAT Mod jars plus the stable config/defaultconfig set under `build/release-smoke` so the release artifacts can be inspected outside the dev classpath.

`validateStandaloneReleaseServerSmoke` installs a standalone Forge server under `build/standalone-server`, injects the staged release bundle plus the mandatory local Epic Fight runtime jars from `libs/` / `../STAT_MOD/libs`, and verifies the boot reaches `Done` plus `STAT Mod ready on server`.

`validateStandaloneReleaseClientSmoke` installs a standalone Forge client under `build/standalone-client`, injects the staged release bundle plus the mandatory local Epic Fight runtime jars from `libs/` / `../STAT_MOD/libs`, reuses the local ForgeGradle asset cache by default, and verifies the boot reaches `Setting user: Dev`, `STAT Mod loaded!`, `Sihriya chargé avec Epic Fight + STAT Mod !`, and `OpenAL initialized`.

`runGameTestServer -PforgeRunDir=run-gametest --no-daemon` runs the runtime gameplay smoke suite against a clean GameTest server boot. The current suite proves the live school/spell flow, unlock evaluation, and ranked spell selection against the real registry data.

## CI

The GitHub Actions workflow in `.github/workflows/build.yml` runs the same Gradle build used locally.

Set repository variable `STAT_MOD_REPOSITORY` to the `owner/repo` value of the STAT Mod repository so CI can check out the sibling project beside Sihriya.

## Distribution notes

Sihriya is an addon, not a standalone magic mod. Release packages must keep `mods.toml` and Gradle dependencies aligned with that runtime contract.

Sihriya should not force optional Epic Fight addons into the default runtime. Addons such as Nightfall, Avalon, P1nero Bow, Weapons of Miracles, ParCool, Epic Parcool, or Epic Fight Extra should be added deliberately at modpack level after dedicated-server testing.

See:
- `docs/verification-warning-baseline.md`
- `docs/client-smoke-warning-baseline.md`
- `docs/pro-readiness-audit.md`
- `docs/release-checklist.md`
- `GUIDE-DEV.md`

The license declared in `gradle.properties` is `All Rights Reserved`; read `LICENSE` before redistributing or modifying this project outside the owner's authorized workspace.
