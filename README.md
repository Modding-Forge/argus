# Argus

Argus Panoptes, the all-seeing giant of Greek mythology, had a hundred eyes and missed nothing.

Argus is a Sodium-native Fabric client mod for OptiFine resource-pack compatibility on the modern Minecraft renderer path. No bridge mod, no wrapper layer, no old renderer detour: Argus implements the relevant resource-pack features directly where they render.

[![Get it on Modrinth](https://raw.githubusercontent.com/Modding-Forge/mf-nexus-modpage-assets/main/buttons/get_it_on_modrinth.png)](https://modrinth.com/mod/argus) [![Get it on Nexus Mods](https://raw.githubusercontent.com/Modding-Forge/mf-nexus-modpage-assets/main/buttons/get_it_on_nexus_mods.png)](https://www.nexusmods.com/minecraft/mods/1183) [![Get it on CurseForge](https://raw.githubusercontent.com/Modding-Forge/mf-nexus-modpage-assets/main/buttons/get_it_on_curseforge.png)](https://www.curseforge.com/minecraft/mc-mods/argus/)

[![Join us on Discord](https://raw.githubusercontent.com/Modding-Forge/mf-nexus-modpage-assets/main/buttons/join_us_on_discord.png)](https://discord.gg/pqEHdWDf8z)

This is a stable beta. The core systems work in-game, most supported OptiFine pack features should work with existing packs, and compatibility bugs are being fixed against real-world packs as they show up.

Entity texture features are included for the OptiFine scope. Entity model and entity animation packs are not included; use EMF/ETF for that stack. When ETF is installed, Argus disables its overlapping entity texture features and greys out the related settings.

## Why One Mod?

OptiFine-style features share a lot of infrastructure: pack discovery, `.properties` parsing, condition matching, reload snapshots, atlas injection, renderer lookups, compatibility policy, and settings UI.

Argus keeps those shared parts in one clean-room implementation instead of duplicating them across several single-feature mods. That means fewer competing hooks, fewer duplicated caches, and one coherent Sodium-facing renderer path.

## Features

Implemented or actively usable:

- Connected Textures: CTM, compact CTM, horizontal, vertical, top, fixed, random, repeat, layered methods, overlays, and atlas tile injection.
- Built-in regular-glass CTM resource pack.
- Better Grass and Better Snow.
- Emissive textures for block and CTM textures.
- Custom Item Textures for `type=item`.
- Custom GUI textures for OptiFine-style container rules.
- Custom Animations MVP for block-atlas texture animation rules.
- Custom Colors and Colormaps.
- Custom Sky layers.
- Natural Textures via `optifine/natural.properties`.
- OptiFine-scope Entity Textures: random entities, random tile entities, paintings, chest/shulker hooks, layer synchronization, and emissive companions.
- Sodium settings integration for Argus features and visual controls.
- ETF compatibility: overlapping Argus entity texture features automatically stand down.
- EMF compatibility: Argus does not own entity models.
- Dynamic Lighting mod compatibility.

Not implemented:

- Entity model replacement and entity animation packs, including Fresh Animations-style CEM.
- Dynamic Lighting. Use a dedicated Dynamic Lighting mod alongside Argus.
- NeoForge support.

## Status

Argus targets real OptiFine resource packs, not synthetic demos only. CTM, Better Grass, Emissive, Custom Colors, Custom Sky, Custom Animations, Custom GUI, CIT, and Entity Texture support have all reached visible in-game paths, but parity is still being tightened feature by feature.

Fabric/Sodium is the supported runtime today. NeoForge is planned once the Minecraft 26.2 toolchain is ready.

The current terrain path is a correctness-oriented Sodium quad path. It is not claimed as a final Vulkan-native renderer implementation.

For detailed phase status, see `plan/roadmap.md`.

## Requirements

- Minecraft `26.2`
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

Local test resource packs go into `src/fabric/run/resourcepacks/`.

## Project Layout

- `src/shared`: loader-agnostic parsers, config, feature models, matching logic, and JVM tests.
- `src/fabric`: Fabric entrypoints, reload listeners, Minecraft adapters, Mixins, Sodium integration, atlas injection, and settings UI.
- `src/neoforge`: TODO stub only.
- `plan/`: roadmap and implementation notes.

## Clean-Room Policy

Argus aims for OptiFine-visible resource-pack behavior without copying OptiFine code. OptiFine may be used only as behavioral reference. Do not copy or transliterate OptiFine implementation code, identifiers, class layouts, method bodies, or internal control flow into this repository.

## License

Argus is licensed under the PolyForm Shield License 1.0.0. See [LICENSE](LICENSE).
