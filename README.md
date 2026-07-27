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

| Command | Description |
| --- | --- |
| `/simpleframes doShearsBreak <true\|false>` | Whether shears take durability damage / break when used. |
| `/simpleframes doLeatherFix <true\|false>` | Whether invisible frames can be restored with leather. |

Running a subcommand without a value prints the current setting.

**Permissions.** The command is guarded by the node `simpleframes.command`. If [fabric-permissions-api](https://modrinth.com/mod/fabric-permissions-api) (e.g. via LuckPerms) is installed, grant that node; otherwise it falls back to vanilla operator **level 3**.

## Configuration

Config file: `config/SimpleFrames.conf`

```properties
# Do shears get damaged and break
doShearsBreak=true

# True if you want to reverse invisible frames back with leather
fixWithLeather=true
```

## Requirements

- Minecraft **1.18 – 26.2** (every version in between is supported)
- [Fabric Loader](https://fabricmc.net/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)

## Supported versions

Built from a single source with [Stonecutter](https://stonecutter.kikugie.dev/): one jar per build anchor, each covering a patch band. Every Minecraft version from 1.18 to 26.2 is published with no gaps.

| Build anchor | Covers | Java |
| --- | --- | --- |
| 1.18.2 | 1.18 – 1.18.2 | 17 |
| 1.19.4 | 1.19 – 1.19.4 | 17 |
| 1.20.4 | 1.20 – 1.20.4 | 17 |
| 1.20.6 | 1.20.5 – 1.20.6 | 21 |
| 1.21.1 | 1.21 – 1.21.1 | 21 |
| 1.21.8 | 1.21.2 – 1.21.8 | 21 |
| 1.21.10 | 1.21.9 – 1.21.11 | 21 |
| 26.1.2 | 26.1 – 26.1.2 | 25 |
| 26.2 | 26.2 | 25 |

## Building

Requires a JDK matching the newest target you build (**JDK 25** to build the 26.x anchors; JDK 17/21 build only the yarn anchors — the 26.x nodes are auto-skipped below JDK 25).

```bash
./gradlew build            # builds every version
./gradlew ":1.21.8:build"  # builds a single anchor
```

Each anchor's jar lands in `versions/<anchor>/build/libs/`.

## Server plugin (Bukkit / Spigot / Paper / Purpur / Folia)

The same behaviour is also available as a **server plugin** (no client mod needed on either side) in [`plugin/`](plugin) — a standalone Gradle build using the Bukkit API. One jar runs on Spigot, Paper, Purpur and Folia (`folia-supported`), across MC 1.18+.

```bash
./gradlew -p plugin build       # build + tests (MockBukkit)
./gradlew -p plugin runServer   # boot a Paper server with the plugin (-Prun_mc=1.21.8)
```

Same features (shear → invisible, leather → restore, persistence through breaking, `doShearsBreak`/`fixWithLeather` config, `/simpleframes` command gated by `simpleframes.command`). The plugin jar lands in `plugin/build/libs/`.

## License

[MIT](LICENSE) © iSlavok
