# Client Smoke Warning Baseline

This file documents the currently tolerated warning baseline for the clean client smoke run `run-client-clean/logs/latest.log`.

The goal is strict: local client regressions should fail `validateClientSmokeLog`. Only upstream noise that is currently outside this workspace should remain tolerated.

The following local regressions are explicitly not tolerated:

- `Invalid path in pack`
- `Datapack animation reading failed`
- `Unable to load model`
- `Missing textures in model statmod:class_arts#inventory`

## Allowed warning lines

The current clean client smoke run may still emit these upstream mixin warnings:

1. `Error loading class: dev/tr7zw/skinlayers/versionless/render/CustomModelPart ...`
2. `Error loading class: dev/tr7zw/skinlayers/versionless/render/CustomizableCube ...`
3. `Error loading class: de/teamlapen/vampirism/client/renderer/entity/layers/VampirePlayerHeadLayer ...`
4. `Error loading class: de/teamlapen/werewolves/client/render/layer/HumanWerewolfLayer ...`

These are treated as third-party optional-target mixin noise, not as local `SIHRIYA` or `STAT_MOD` regressions.

## Regressions that are not tolerated

The client smoke gate must fail if any of these return:

- `Invalid path in pack`
- `Datapack animation reading failed`
- `Unable to load model`
- client/common config autocorrection warnings
- `Missing data pack mod:`
- `InputConstants$Key`
- unexpected crash markers or client boot exceptions

## Required success markers

The clean client smoke log must still show:

- `Setting user: Dev`
- `[statmod/]: STAT Mod loaded!`
- `[sihriya/]: Sihriya chargé avec Epic Fight + STAT Mod !`
- `OpenAL initialized`
