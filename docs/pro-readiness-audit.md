# Pro Readiness Audit

This document records what is currently proven, what is tolerated as upstream debt, and what is still not fully proven for the objective `faire de ce mod un mod pro`.

It is intentionally strict. A green build is evidence, not a blanket claim that every production concern has been closed.

## Proven now

### Build and packaging

- `.\gradlew.bat build` succeeds for the current `SIHRIYA + STAT_MOD` workspace
- `validateReleaseArtifacts` proves the current reobfuscated jars and staged release-smoke bundle contain the expected runtime payloads
- `validateStandaloneReleaseServerSmoke` proves the staged release-smoke bundle plus mandatory local Epic Fight runtime jars can boot in a standalone Forge server install
- `validateStandaloneReleaseClientSmoke` proves the staged release-smoke bundle plus mandatory local Epic Fight runtime jars can boot in a standalone Forge client install when a local asset cache is available
- `validateSihriyaContent` proves the current content contract:
  - 9 schools
  - 252 spells
  - translation/assets/animation references checked
- `validateSihriyaPackaging` proves:
  - Forge metadata exists and is shaped as expected
  - dependency declarations are present
  - root release documents exist
  - multiple server-hardening invariants are still present in source
- `..\STAT_MOD\gradlew.bat validateStatModPackaging` proves the sibling project still enforces its current packaging/runtime guards

### Dedicated-server verification

- `runServer -PforgeRunDir=run-clean --no-daemon` has been verified to reach:
  - `DedicatedServer/]: Done`
  - `[statmod/]: STAT Mod ready on server`
- a standalone Forge server install boot from `build/release-smoke/mods` has been verified to reach:
  - `DedicatedServer/]: Done`
  - `[statmod/]: STAT Mod ready on server`
- `validateStandaloneReleaseServerSmoke` encodes that standalone-server proof instead of leaving it manual
- `validateStandaloneReleaseClientSmoke` encodes the standalone-client boot proof instead of leaving it manual
- `validateVerificationServerLog` proves the clean verification boot does not regress on previously fixed local issues:
  - no `Missing data pack mod:`
  - no `InputConstants$Key`
  - no server-config autocorrection warnings
  - no missing `statmod`
  - no port bind conflict in the verification setup
  - no missing EULA in verification setup

### Client smoke verification

- `runClient -PforgeRunDir=run-client-clean --no-daemon` has been verified to reach:
  - `Setting user: Dev`
  - `[statmod/]: STAT Mod loaded!`
  - `[sihriya/]: Sihriya chargé avec Epic Fight + STAT Mod !`
  - `OpenAL initialized`
- a standalone Forge client install boot from `build/release-smoke/mods` has been verified to reach:
  - `Setting user: Dev`
  - `[statmod/]: STAT Mod loaded!`
  - `[sihriya/]: Sihriya chargé avec Epic Fight + STAT Mod !`
  - `OpenAL initialized`
- `validateClientAnimationPackaging` proves Sihriya only packages registered Epic Fight-scanned animation payloads
- `validateClientSmokeLog` proves the clean client smoke boot does not regress on previously fixed local issues:
  - no `Invalid path in pack`
  - no `Datapack animation reading failed`
  - no missing item models for local `STAT_MOD` assets
  - no first-run client config autocorrection warnings in the prepared smoke setup

### Defensive engineering already encoded

Current source/build evidence covers at least these areas:
- network input rules and bounded packet decoding
- temporary summon/wall safety rules
- cooldown tracker cleanup
- progression save sanitation
- representative legacy NBT migration fixtures across Sihriya school progression and STAT Mod player capability managers
- immutable registry and client-cache views
- verification-run preparation with stable server defaults
- clean client-smoke preparation with stable config defaults
- documented warning baseline and release checklist
- encoded standalone release-server smoke validation

## Tolerated upstream debt

These warnings still appear in the clean verification boot and are currently treated as tolerated upstream noise rather than local regressions:

1. Forge language jars without `mods.toml`
2. Epic Fight `MixinWitherBoss` `@Final` warnings
3. Epic Fight `defineId called for` warnings
4. Forge `unexpected schema` warnings for `.mcassetsroot`

The current authoritative baseline is documented in `docs/verification-warning-baseline.md` and enforced by `validateVerificationServerLog`.
The current authoritative client-smoke baseline is documented in `docs/client-smoke-warning-baseline.md` and enforced by `validateClientSmokeLog`.

## Not yet fully proven

The following items are not yet proven strongly enough to call the whole mod fully “pro” without reservation:

1. Gameplay workflow validation
   - spell casting, progression unlocks, mana loop, and STAT Mod interplay are covered indirectly by code/tests
   - they are not yet all proven by an explicit end-to-end gameplay smoke suite

2. Upgrade/migration confidence
   - save sanitation exists
   - representative legacy NBT fixtures are now covered for Sihriya school progression and STAT Mod player capability managers
   - real-world upgrade testing from older live player/server data is still not yet proven

3. Performance envelope
   - several memory/state leaks and static-state accumulations were addressed
   - no explicit performance budget or regression threshold is currently enforced

## Current conclusion

Current state is materially closer to a professional mod than the starting state:
- stronger runtime boundaries
- better dedicated-server hygiene
- better release/process documentation
- repeatable verification for previously fixed regressions

But the evidence still supports this narrower claim:

> The mod now has strong server-side, standalone-server, standalone-client, and client-boot verification baselines plus significantly improved packaging/runtime hygiene, but it is not yet fully proven as a complete “pro” release across gameplay smoke, migration, and performance characterization.
