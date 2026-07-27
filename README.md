# Simple Frames

A lightweight server-side Fabric mod that lets you make item frames **invisible** with shears — leaving only the item floating on your wall.

## Features

- ✂️ **Shear an item frame** to make it invisible. The frame stays fully functional — only its border disappears.
- 🧵 **Restore it with leather** (optional). Right-click... err, *hit* an invisible frame with leather to bring the border back.
- 🪄 **Persists on pickup.** Break an invisible frame and it drops as a special item that keeps its invisibility when placed again.
- ✨ **Glow item frames supported.** Vanilla glow frames can be made invisible too — the item keeps its fullbright glow, only the frame vanishes.
- ⚙️ **Configurable.** Toggle whether shears take damage and whether leather restoration is enabled — via config file or in-game command.
- 🖥️ **Server-side.** No client mod required for other players to see the effect.

## Usage

1. Place an item (or leave it empty) in a normal or glow item frame.
2. Hit the frame with **shears** → the frame becomes invisible.
3. To undo, hit the invisible frame with **leather** (if leather restoration is enabled).

## Commands

Requires permission level 3 (operator).

| Command | Description |
| --- | --- |
| `/simpleframes doShearsBreak <true\|false>` | Whether shears take durability damage / break when used. |
| `/simpleframes doLeatherFix <true\|false>` | Whether invisible frames can be restored with leather. |

Running a subcommand without a value prints the current setting.

## Configuration

Config file: `config/SimpleFrames.conf`

```properties
# Do shears get damaged and break
doShearsBreak=true

# True if you want to reverse invisible frames back with leather
fixWithLeather=true
```

## Requirements

- Minecraft **1.21.4**
- [Fabric Loader](https://fabricmc.net/) `>=0.14.8`
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) `>=1.12.0`

## Building

The mod builds against **JDK 21** (the correct toolchain for Minecraft 1.21.4). The Gradle daemon is pinned to JDK 21 via `gradle/gradle-daemon-jvm.properties`, so a plain build works even if your system default JDK is newer:

```bash
./gradlew build
```

The built jar lands in `build/libs/`.

## License

[MIT](LICENSE) © iSlavok
