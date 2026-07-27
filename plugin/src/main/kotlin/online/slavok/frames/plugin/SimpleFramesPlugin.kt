package online.slavok.frames.plugin

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

// Non-final: MockBukkit subclasses the main class (ByteBuddy) in the load test.
open class SimpleFramesPlugin : JavaPlugin() {

    /** PDC key marking a frame (entity) or a dropped frame item as mod-invisible. */
    lateinit var invisibleKey: NamespacedKey
        private set

    var doShearsBreak = true
        private set
    var fixWithLeather = true
        private set

    override fun onEnable() {
        saveDefaultConfig()
        doShearsBreak = config.getBoolean("doShearsBreak", true)
        fixWithLeather = config.getBoolean("fixWithLeather", true)
        invisibleKey = NamespacedKey(this, "invisibleframe")

        server.pluginManager.registerEvents(FrameListener(this), this)
        getCommand("simpleframes")?.setExecutor(FramesCommand(this))

        logger.info("Simple Frames enabled")
    }

    fun setDoShearsBreak(value: Boolean) {
        doShearsBreak = value
        config.set("doShearsBreak", value)
        saveConfig()
    }

    fun setFixWithLeather(value: Boolean) {
        fixWithLeather = value
        config.set("fixWithLeather", value)
        saveConfig()
    }
}
