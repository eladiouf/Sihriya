# Verification Warning Baseline

This file documents the warning categories currently tolerated during the dedicated-server verification boot on `run-clean/logs/latest.log`.

The purpose is narrow:
- local regressions must fail the build
- known upstream Forge / Epic Fight warnings may remain temporarily
- any new warning category on the verification boot must be reviewed before being accepted

## Allowed warning categories

1. Forge language jars without `mods.toml`
   - `javafmllanguage`
   - `lowcodelanguage`
   - `mclanguage`
   - `fmlcore`

2. Epic Fight mixin metadata warning
   - `MixinWitherBoss` `@Final` field warnings

3. Epic Fight entity data registration warnings
   - `defineId called for` on:
     - `LivingEntityPatch`
     - `PlayerPatch`
     - `WitherPatch`

4. Forge pack URL schema warning
   - `unexpected schema` on Forge `assets/.mcassetsroot`
   - `unexpected schema` on Forge `data/.mcassetsroot`

## Explicitly not allowed in verification boot

These are treated as local regressions and must fail validation:
- `Missing data pack mod:`
- `InputConstants$Key`
- server config auto-correction warnings
- `statmod is not installed`
- port bind failure
- missing EULA
- missing `Done`
- missing `STAT Mod ready on server`

## Source of truth

The build task `validateVerificationServerLog` in `build.gradle` is authoritative.

If a warning disappears upstream, remove it from the whitelist.
If a new warning appears, do not silently accept it: classify it first, then either fix it locally or add a justified baseline update here and in the validator.
