# Simple Frames

A lightweight server-side Fabric mod that lets you make item frames **invisible** with shears — leaving only the item floating on your wall — and **wax them with honeycomb** to lock the item in place.

## Features

- ✂️ **Shear an item frame** to make it invisible. The frame stays fully functional — only its border disappears.
- 🧵 **Restore it with leather** (optional). Right-click... err, *hit* an invisible frame with leather to bring the border back.
- 🪄 **Persists on pickup.** Break an invisible frame and it drops as a special item that keeps its invisibility when placed again.
- ✨ **Glow item frames supported.** Vanilla glow frames can be made invisible too — the item keeps its fullbright glow, only the frame vanishes.
- 🍯 **Wax a frame with honeycomb** to lock the item's rotation — it can no longer be rotated or swapped, just like waxing a sign. Only frames that already hold an item can be waxed.
- 🪓 **Un-wax with any axe.** Right-click a waxed frame with an axe to remove the wax (the axe takes durability, vanilla-style).
- 🔒 **Optional full lock.** By default waxing only blocks right-click interactions; enable `waxFullLock` to also make a waxed frame invulnerable.
- ⚙️ **Configurable.** Toggle shear damage, leather restoration, and every part of the wax feature — via config file or in-game command.
- 🖥️ **Server-side.** No client mod required for other players to see the effect.

## Usage

1. Place an item (or leave it empty) in a normal or glow item frame.
2. Hit the frame with **shears** → the frame becomes invisible.
3. To undo, hit the invisible frame with **leather** (if leather restoration is enabled).

**Waxing** (independent of invisibility — the two combine):

1. Put an item in a frame, then right-click it with **honeycomb** → the item's rotation is locked. Trying to rotate or swap it plays a denied click.
2. Right-click the waxed frame with **any axe** → the wax comes off.

Wax is not kept when the frame is broken. Consumption is vanilla-style (one honeycomb to wax, axe durability to un-wax; free in creative).

## Commands

| Command | Description |
| --- | --- |
| `/simpleframes doShearsBreak <true\|false>` | Whether shears take durability damage / break when used. |
| `/simpleframes doLeatherFix <true\|false>` | Whether invisible frames can be restored with leather. |
| `/simpleframes enableWax <true\|false>` | Master toggle for the whole wax feature. |
| `/simpleframes waxFullLock <true\|false>` | `false` blocks right-click only; `true` also makes a waxed frame invulnerable. |
| `/simpleframes doAxeBreak <true\|false>` | Whether axes take durability damage when removing wax. |

Running a subcommand without a value prints the current setting.

**Permissions.** The command is guarded by the node `simpleframes.command`. If [fabric-permissions-api](https://modrinth.com/mod/fabric-permissions-api) (e.g. via LuckPerms) is installed, grant that node; otherwise it falls back to vanilla operator **level 3**.

## Configuration

Config file: `config/SimpleFrames.conf`

```properties
# Do shears get damaged and break
doShearsBreak=true

# True if you want to reverse invisible frames back with leather
fixWithLeather=true

# Enable the wax feature: honeycomb locks an item's rotation, an axe unlocks it
enableWax=true

# false = waxing blocks only right-click; true = a waxed frame is also invulnerable
waxFullLock=false

# Do axes get damaged when removing wax
doAxeBreak=true
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

## Server plugin (Paper / Purpur / Folia)

The same behaviour is also available as a **server plugin** (no client mod needed on either side) in [`plugin/`](plugin) — a standalone Gradle build on the Paper API. One jar runs on Paper, Purpur and Folia (`folia-supported`), across MC 1.18+. (Paper-family only: it uses the Adventure API so the item name renders non-italic on every version — pure Spigot doesn't bundle Adventure.)

```bash
./gradlew -p plugin build       # build + tests (MockBukkit)
./gradlew -p plugin runServer   # boot a Paper server with the plugin (-Prun_mc=1.21.8)
```

Same features (shear → invisible, leather → restore, honeycomb → wax, axe → un-wax, persistence through breaking, the full `doShearsBreak`/`fixWithLeather`/`enableWax`/`waxFullLock`/`doAxeBreak` config, `/simpleframes` command gated by `simpleframes.command`). The plugin jar lands in `plugin/build/libs/`.

## License

[MIT](LICENSE) © iSlavok
