package online.slavok.frames

import net.fabricmc.loader.api.FabricLoader
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class Config {
    private val configFile: File =
        FabricLoader.getInstance().configDir.resolve("SimpleFrames.conf").toFile()

    // Do shears get damaged and break
    @JvmField
    var doShearsBreak = true

    // True if you want to reverse invisible frames back with leather
    @JvmField
    var fixWithLeather = true

    // Enable the wax feature: honeycomb locks an item's rotation, an axe unlocks it.
    @JvmField
    var enableWax = true

    // false = waxing blocks only right-click (rotation / item change);
    // true  = a waxed frame is also fully invulnerable (item can't be knocked out, frame can't break).
    @JvmField
    var waxFullLock = false

    // Do axes lose durability when removing wax.
    @JvmField
    var doAxeBreak = true

    // Which mouse button triggers each interaction: LEFT, RIGHT, or BOTH.
    @JvmField
    var shearsButton = ClickMode.LEFT

    @JvmField
    var leatherButton = ClickMode.LEFT

    @JvmField
    var honeycombButton = ClickMode.BOTH

    @JvmField
    var axeButton = ClickMode.BOTH

    // Do leather / honeycomb get consumed when used.
    @JvmField
    var doLeatherConsume = true

    @JvmField
    var doHoneycombConsume = true

    // Item names shown on the invisible-frame item.
    @JvmField
    var invisibleFrameName = "Invisible Item Frame"

    @JvmField
    var invisibleGlowFrameName = "Invisible Glow Item Frame"

    fun load() {
        try {
            val br = BufferedReader(FileReader(configFile))
            var line = br.readLine()
            while (line != null) {
                if (!line.startsWith("#") && line.contains("=")) {
                    val idx = line.indexOf("=")
                    val key = line.substring(0, idx).trim()
                    var value = line.substring(idx + 1)
                    if (value.contains("#")) value = value.substring(0, value.indexOf("#"))
                    value = value.trim()
                    when (key) {
                        "doShearsBreak" -> doShearsBreak = value.toBoolean()
                        "fixWithLeather" -> fixWithLeather = value.toBoolean()
                        "enableWax" -> enableWax = value.toBoolean()
                        "waxFullLock" -> waxFullLock = value.toBoolean()
                        "doAxeBreak" -> doAxeBreak = value.toBoolean()
                        "shearsButton" -> shearsButton = ClickMode.parse(value, ClickMode.LEFT)
                        "leatherButton" -> leatherButton = ClickMode.parse(value, ClickMode.LEFT)
                        "honeycombButton" -> honeycombButton = ClickMode.parse(value, ClickMode.BOTH)
                        "axeButton" -> axeButton = ClickMode.parse(value, ClickMode.BOTH)
                        "doLeatherConsume" -> doLeatherConsume = value.toBoolean()
                        "doHoneycombConsume" -> doHoneycombConsume = value.toBoolean()
                        "invisibleFrameName" -> if (value.isNotEmpty()) invisibleFrameName = value
                        "invisibleGlowFrameName" -> if (value.isNotEmpty()) invisibleGlowFrameName = value
                    }
                }
                line = br.readLine()
            }
            br.close()
        } catch (e: IOException) {
            SimpleFramesMod.LOGGER.warn("Error on Config.load() .conf > " + e.message)
            dump()
        }
    }

    fun dump() {
        try {
            SimpleFramesMod.LOGGER.info("Generating brand new .conf file...")
            val writer = FileWriter(configFile)
            writer.write("# Do shears get damaged and break\n")
            writer.write("doShearsBreak=$doShearsBreak\n\n")
            writer.write("# True if you want to reverse invisible frames back with leather\n")
            writer.write("fixWithLeather=$fixWithLeather\n\n")
            writer.write("# Enable the wax feature (honeycomb locks item rotation, axe unlocks)\n")
            writer.write("enableWax=$enableWax\n\n")
            writer.write("# false = block only rotation/right-click; true = waxed frame is fully invulnerable\n")
            writer.write("waxFullLock=$waxFullLock\n\n")
            writer.write("# Do axes lose durability when removing wax\n")
            writer.write("doAxeBreak=$doAxeBreak\n\n")
            writer.write("# Which mouse button triggers each interaction: LEFT, RIGHT, or BOTH.\n")
            writer.write("# Shears/leather on RIGHT (or BOTH): sneak + right-click = vanilla place/rotate,\n")
            writer.write("# plain right-click = the mod action.\n")
            writer.write("shearsButton=${shearsButton.name}\n")
            writer.write("leatherButton=${leatherButton.name}\n")
            writer.write("honeycombButton=${honeycombButton.name}\n")
            writer.write("axeButton=${axeButton.name}\n\n")
            writer.write("# Do leather / honeycomb get consumed when used\n")
            writer.write("doLeatherConsume=$doLeatherConsume\n")
            writer.write("doHoneycombConsume=$doHoneycombConsume\n\n")
            writer.write("# Item name shown on an invisible frame\n")
            writer.write("invisibleFrameName=$invisibleFrameName\n\n")
            writer.write("# Item name shown on an invisible glow frame\n")
            writer.write("invisibleGlowFrameName=$invisibleGlowFrameName")
            writer.close()
            SimpleFramesMod.LOGGER.info("Simple Frames Config file created with path: " + configFile.absolutePath)
        } catch (e: IOException) {
            SimpleFramesMod.LOGGER.error("Error on Config.dump() > " + e.message)
        }
    }
}
