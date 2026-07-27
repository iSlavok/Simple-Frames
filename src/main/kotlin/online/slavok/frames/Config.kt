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

    fun load() {
        try {
            val br = BufferedReader(FileReader(configFile))
            var line = br.readLine()
            while (line != null) {
                if (!line.startsWith("#") && line.contains("=")) {
                    line = line.replace(" ", "")
                    val key = line.substring(0, line.indexOf("="))
                    var value = line.substring(line.indexOf("=") + 1)
                    if (value.contains("#")) value = value.substring(0, value.indexOf("#"))
                    when (key) {
                        "doShearsBreak" -> doShearsBreak = value.toBoolean()
                        "fixWithLeather" -> fixWithLeather = value.toBoolean()
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
            writer.write("fixWithLeather=$fixWithLeather")
            writer.close()
            SimpleFramesMod.LOGGER.info("Simple Frames Config file created with path: " + configFile.absolutePath)
        } catch (e: IOException) {
            SimpleFramesMod.LOGGER.error("Error on Config.dump() > " + e.message)
        }
    }
}
