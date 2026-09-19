# NoDelay

Client-side Minecraft mod that selectively removes the vanilla right-click
delay (`Minecraft#rightClickDelay`). Instead of one global switch you get
**per-category control**: blocks, villagers, entities and items.

Works on **Fabric, Quilt, Forge and NeoForge** for Minecraft **26.1-26.3**
and **1.21-1.21.11**. Client-side only - works on vanilla servers, nothing
needed on the server.

## Features

- Removes the placement delay per category (each `0-20` ticks, vanilla is `4`)
- Categories: `blocks` (placing), `villagers` (trading/hiring), `entities`
  (buttons, loot, doors...), `items` (using items in air)
- Default: `blocks` and `villagers` enabled with `0` ticks; `entities` and
  `items` disabled (vanilla 4-tick delay)
- In-game command `/nodelay` (on/off, reload, per-category on/off/delay)
- Config file: `config/nodelay.json` (auto-generated, clamped to `0..20`)
- Optional dependency: Fabric API (for the `/nodelay` command on Fabric/Quilt only)
- Localized into **14 languages**, follows your game language
- Java 21+ for 1.21.x builds, Java 25+ for 26.x builds

## Supported versions

| Loader    | Minecraft                                | Files        |
|-----------|------------------------------------------|--------------|
| Fabric    | 26.1-26.3                                | 1 jar        |
| Fabric    | 1.21-1.21.11                             | 1 jar        |
| Quilt     | 26.1-26.3                                | 1 jar        |
| Forge     | 1.21, 1.21.1, 1.21.3-1.21.11, 26.2      | 1 jar each   |
| NeoForge  | 1.21, 1.21.1, 1.21.3-1.21.11, 26.1-26.3  | 1 jar each   |

## Config

`config/nodelay.json`:

```json
{
  "enabled": true,
  "categories": {
    "blocks":    { "enabled": true,  "delay": 0 },
    "villagers": { "enabled": true,  "delay": 0 },
    "entities":  { "enabled": false, "delay": 4 },
    "items":     { "enabled": false, "delay": 4 }
  }
}
```

## Commands (client-side, require Fabric API on Fabric/Quilt)

```
/nodelay                               show current state
/nodelay on|off                        enable / disable everything
/nodelay reload                        re-read the config file
/nodelay blocks|villagers|entities|items on|off
/nodelay blocks|villagers|entities|items delay <0-20>
```

## How it works

`Minecraft#startUseItem()` writes `rightClickDelay = 4`, `Minecraft#tick()`
decrements it once per tick, and the use button is only handled again when it
reaches `0` - that is the "delay" you feel between two block placements.

A tiny Mixin (`NoDelayMixin`) is hooked right onto that field write
(`PUTFIELD rightClickDelay:I`, immediately after it) and overwrites it with
the configured value. The target category is chosen from the current `hitResult`
(a block, a villager, another entity, or air). The hook is placed on the field
write rather than on the method's tail because `startUseItem()` has several
early returns.

## Build

Each per-loader project is a separate Gradle project:

```bash
cd <project>  # e.g. nodelay-26, nodelay-1.21, or a forge/neoforge/quilt project
.\gradlew.bat build
```

The resulting jar is placed in `build/libs/`.

## Install

1. Install the matching loader (Fabric, Quilt, Forge or NeoForge) for your
   Minecraft version.
2. Drop the matching `.jar` into the `mods/` folder.
3. (Optional) Install [Fabric API](https://modrinth.com/mod/fabric-api) if you
   want the `/nodelay` command on Fabric/Quilt builds.

## License

Licensed under [CC0-1.0](https://creativecommons.org/publicdomain/zero/1.0/) -
do whatever you want with it.

## Download

- [Modrinth](https://modrinth.com/mod/nodelay-mod)
- [GitHub Releases](https://github.com/c4elovek-cmd/NoDelay/releases)