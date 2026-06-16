# Argus

Argus Panoptes, the all-seeing giant of Greek mythology, had a hundred eyes and missed nothing.

Argus is a Sodium-native client mod that brings OptiFine resource pack compatibility to Fabric, built from the ground up for the modern Minecraft renderer. No bridges, no wrapper layers, no compatibility hacks: just direct integration with the rendering paths where the features actually live.

This is a stable beta release. The core feature set works in-game, and most supported OptiFine resource-pack features should work for real texture packs without pack changes. Your existing OptiFine resource packs are the target: install the pack, enable Argus, and keep building on the same assets.

Entity texture features are included for the OptiFine scope, but new entity models and entity animations are not part of Argus. Use EMF and ETF for that stack. When ETF is installed, Argus automatically disables its overlapping Entity Texture Features and greys out the related settings, so the two setups can coexist cleanly.

The current implementation is Fabric first and requires Sodium. Shared feature logic lives in `src/shared`; Fabric, Minecraft, resource reload, Mixin, and Sodium integration live in `src/fabric`. `src/neoforge` is intentionally a TODO stub until a suitable Minecraft 26.2 NeoForge toolchain exists.

## Why One Mod For Many Features?

Argus intentionally implements multiple OptiFine-style visual systems in one clean-room mod instead of treating CTM, Better Grass, Emissive Textures, CIT, and Custom GUI as unrelated islands.

That shape matters because these features share real infrastructure: resource-pack discovery, properties parsing, condition matching, reload snapshots, atlas injection, renderer-safe lookup tables, compatibility policy, config UI, and hot-path prefilters. Reusing those parts keeps behavior more consistent, reduces duplicate renderer hooks, and gives Argus room to optimize across feature boundaries.

The goal is not to bundle features for the sake of size. The goal is a clean reimplementation where shared systems are designed once, tested once, and reused by every feature that needs them. Compared with stacking several equivalent single-feature mods, that can mean fewer duplicated caches, fewer competing Mixins, more stable priority rules, and a clearer path toward Sodium- and future-renderer-friendly data flow.

## Features

- Connected Textures support for OptiFine-style CTM resource-pack rules, including 47-tile CTM, compact CTM, horizontal, vertical, top, fixed, random, repeat, layered methods, overlays, and CTM tile atlas injection.
- Better Grass support with Vanilla block families, `optifine/bettergrass.properties`, resource-pack override handling, Sodium menu controls, snow handling, dirt path and farmland lowered-block support, and an optional resource-pack ignore switch.
- Emissive texture support for block textures and CTM tile textures, including compact CTM border packs with `_e` tile companions.
- Custom Item Texture support for `type=item` rules, item texture replacements, model replacements, damage, stack size, enchantment, hand, component, and legacy NBT conditions.
- Custom GUI texture replacement support for OptiFine-style `optifine/gui/container/**/*.properties` rules, including basic screen matching and shulker `colors=` handling.
- Custom Animations MVP for OptiFine-style `optifine/anim/**/*.properties` block-atlas texture rules.
- Custom Colors and Colormaps support for `optifine/color.properties`, `palette.block.*`, `optifine/colormap/**/*.properties`, special colormap PNGs, immutable reload snapshots, hardcoded Vanilla color hooks, and Sodium/Minecraft block tint integration.
- Custom Sky layer support for `optifine/sky/world*/sky*.properties`, including numbered layer scanning, fades, weather/biome/height conditions, transitions, rotation, and common blend modes.
- Natural Textures support for `optifine/natural.properties` and built-in Vanilla-style defaults on Sodium terrain quads.
- Better Snow support with OptiFine-style snow layer coverage for supported non-solid blocks, plus Argus's Better-Grass-owned snow side remap for solid blocks with snow above.
- OptiFine Entity Texture support for Random Entity and Random Tile Entity textures, living-entity layer synchronization, paintings, chest/shulker block-entity texture hooks, reload-time companion texture indexing, and emissive companion passes for supported living-entity textures and generic cutout layers.
- Compatibility handling for ETF and EMF: Argus's overlapping Entity Texture features automatically stand down when Traben's ETF is installed, and Argus keeps CEM/EMF-style model replacement separate so EMF can own entity models.
- Shared condition engine for CIT, Custom GUI, and future condition-based features.
- Fabric client integration through Sodium's terrain quad path and Minecraft's item/GUI rendering paths where those features live.

## Status

Argus is a stable beta: it is not “everything OptiFine ever did,” but the supported foundation is real, tested, and already useful with existing packs. The Fabric/Sodium build runs real resource packs, renders multiple OptiFine-style feature families in-game, and uses a shared parser/matcher/snapshot architecture instead of one-off experiments.

Fabric/Sodium is the supported runtime target today. NeoForge is planned but not part of the build yet. The current terrain renderer path is a correctness-oriented Sodium quad remap path; it is not claimed as a final Vulkan-native implementation.

Implemented or visible today:

- CTM is the most mature feature and the main proof point for the architecture. It is visible in-game with real packs, including PureBDcraft base plus CTM transitions, and has shared unit coverage for rule parsing, resolver planning, prefilters, and 47-tile selection.
- Better Grass is implemented for the main Vanilla families, supports `bettergrass.properties`, and is configurable from the Sodium menu.
  - Full OptiFine parity for every snow/multilayer edge case is still not complete.
- Emissive Textures are implemented and have been verified in-game for the current MVP path, including CTM-related tile companions.
- CIT is partially in-game verified for `type=item` texture/model replacement and already uses the shared condition system plus an O(1) item prefilter.
  - Armor, Elytra, glint overlays, potion filename shortcuts, and broader pack parity are still planned.
- Custom GUI is partially in-game verified. PureBDcraft shulker box GUI colors work through resource-pack rules
  - OptiGUI syntax support, broad Vanilla container testing, and a real fixture suite are still planned.
- Custom Animations are implemented as an MVP for block-atlas texture targets, with shared parser coverage, reload snapshots, a Fabric tick runtime, partial atlas uploads including configurable mipmap distance, a Sodium terrain usage marker, and GUI item cache invalidation for animated item models.
  - Broad pack parity, non-terrain targets, and full in-game smoke coverage are still planned.
- Custom Colors are implemented and visible in-game for the current CPU/runtime path. The feature uses immutable reload snapshots, parser coverage for `color.properties` and colormap rules, Sodium terrain tint hooks, Minecraft item/GUI/entity color hooks, Colormatic fail-safe detection, and a Sodium menu toggle. It supports hard-color tables, aliases, block palette rules, standalone block colormap properties, fixed/vanilla/grid sampling data, decoded colormap PNG snapshots, item grass and potion tints, dye/sheep/collar/sign/XP-bar/text-code colors, durability and XP-orb colormap sampling, map material color overrides, and fog/underwater/underlava color hooks.
  - Full OptiFine parity for every rare hardcoded target, exact biome-column grid semantics, and broader real-pack smoke coverage are still planned. Legacy spawn egg color keys appear obsolete for the current Minecraft item model/tint pipeline and are not treated as a Phase-H completion blocker.
- Custom Sky is implemented for normal OptiFine sky layers and loads real Dramatic Skys-style numbered layer packs through a Minecraft 26.2 sky-renderer path.
  - Exact parity for uncommon blend math, `sun.properties`, `moon_phases.properties`, and broader pack smoke coverage is still planned.
- Natural Textures and Better Snow are implemented on the Sodium terrain path and configurable from the Sodium menu.
  - Their dedicated in-game smoke matrix is still ongoing.
- OptiFine Entity Textures are implemented and in-game verified for the OF feature scope Argus targets: living-entity random base textures, immutable reload snapshots, entity and block-entity render-state context capture, numbered variant discovery, name/biome/height/color/baby/health/moon/day/weather/size/block/profession/legacy-NBT conditions, first layer/overlay synchronization, random paintings, chest/shulker Random Tile Entity hooks, ETF fail-safe detection, and emissive companion rendering for base living-entity models plus generic cutout layers. PureBDcraft Random Entities, random paintings, and synthetic condition/layer/block-entity smoke packs work on this path.
  - When ETF is installed, Argus disables the overlapping Entity Texture runtime and greys out the related Sodium menu entries with an explanation. Argus is also compatible with EMF by keeping entity model replacement separate from its own OptiFine Entity Texture implementation.
  - ETF-only extras, broader stress testing across many third-party packs, and CEM/Fresh Animations parity remain separate work.
- Entity model and entity animation replacement are intentionally not included in this release. Use EMF and ETF for packs such as Fresh Animations; Argus is designed to step aside for ETF where features overlap.

Not implemented yet:

- Entity model replacement and entity animation packs, including Fresh Animations-style CEM support.
- NeoForge support.

See `plan/roadmap.md` for the detailed phase status. The README is intentionally conservative: a feature being implemented does not mean it has complete OptiFine parity.

## Requirements

- Minecraft `26.2-rc-1`
- Java 25
- Fabric Loader
- Fabric API
- Sodium

## Build

```powershell
.\gradlew.bat :src:shared:test
.\gradlew.bat :src:fabric:build
```

The Fabric jar is written to `src/fabric/build/libs/`.

## Run

```powershell
.\gradlew.bat :src:fabric:runClient
```

Install test resource packs into `src/fabric/run/resourcepacks/` for local development runs.

## Project Layout

- `src/shared`: loader-agnostic parsers, CTM engine, Better Grass data model, emissive model, config model, renderer-facing data, and JVM tests.
- `src/fabric`: Fabric entrypoints, resource reload listeners, config path integration, Minecraft adapters, Mixins, Sodium integration, and atlas injection.
- `src/neoforge`: TODO stub only; not included in `settings.gradle`.

## Clean-Room Policy

Argus aims for OptiFine-visible resource-pack behavior without copying OptiFine code. OptiFine may be used only as a behavioral reference. Do not copy or transliterate OptiFine implementation code, identifiers, class layouts, method bodies, or internal control flow into this repository.

## License

Argus is licensed under the PolyForm Shield License 1.0.0. See [LICENSE](LICENSE).
