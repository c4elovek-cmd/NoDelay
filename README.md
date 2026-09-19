# No Placement Delay

Fabric client-side mod for Minecraft that removes the vanilla right-click
placement cooldown (`rightClickDelay`).

![Cover](https://cdn.modrinth.com/data/PICvC7vW/images/placeholder.png)

## Features

- Removes the placement delay (configurable: 0–20 ticks, default 0; vanilla is 4)
- In-game command: `/noplacementdelay on|off|delay <0-20>|reload`
- Config file: `config/noplacementdelay.json`
- Optional dependency: Fabric API (for the command only)
- Localized into 14 languages
- Client-side only — works on vanilla servers

## Supported versions

| Module  | Minecraft       | Java | Requires mappings |
|---------|-----------------|------|-------------------|
| `26.x`  | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | 25 | no (unobfuscated) |
| `1.21`  | 1.21 – 1.21.11  | 21   | Mojang official   |

Two separate Gradle projects are kept in the repository:
`26.x/` (for the 26.1-26.3 line) and `1.21/` (for the 1.21-1.21.11 line).
They are built independently, producing one jar per Minecraft family.

## Build

```bash
# 26.x family
cd 26.x
.\gradlew.bat build

# 1.21 family
cd 1.21
.\gradlew.bat build
```

The resulting jars are placed in `build/libs/`.

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for your
   Minecraft version.
2. Drop the jar into the `mods/` folder.
3. (Optional) Install [Fabric API](https://modrinth.com/mod/fabric-api) if you
   want the command and/or other API-based mods.

## License

This project is licensed under [CC0-1.0](https://creativecommons.org/publicdomain/zero/1.0/) —
do whatever you want with it.

## Download

Available on [Modrinth](https://modrinth.com/mod/noplacementdelay-fabric).