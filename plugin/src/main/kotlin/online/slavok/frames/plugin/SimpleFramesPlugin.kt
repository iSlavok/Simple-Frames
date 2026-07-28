package online.slavok.frames.plugin

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

// Non-final: MockBukkit subclasses the main class (ByteBuddy) in the load test.
open class SimpleFramesPlugin : JavaPlugin() {

    /** PDC key marking a frame (entity) or a dropped frame item as mod-invisible. */
    lateinit var invisibleKey: NamespacedKey
        private set

    /** PDC key marking a frame whose item rotation is waxed/locked. */
    lateinit var waxedKey: NamespacedKey
        private set

    var doShearsBreak = true
        private set
    var fixWithLeather = true
        private set
    var invisibleFrameName = "Invisible Item Frame"
        private set
    var invisibleGlowFrameName = "Invisible Glow Item Frame"
        private set

    var enableWax = true
        private set
    var waxFullLock = false
        private set
    var doAxeBreak = true
        private set

    override fun onEnable() {
        saveDefaultConfig()
        doShearsBreak = config.getBoolean("doShearsBreak", true)
        fixWithLeather = config.getBoolean("fixWithLeather", true)
        invisibleFrameName = config.getString("invisibleFrameName", invisibleFrameName) ?: invisibleFrameName
        invisibleGlowFrameName = config.getString("invisibleGlowFrameName", invisibleGlowFrameName) ?: invisibleGlowFrameName
        enableWax = config.getBoolean("enableWax", true)
        waxFullLock = config.getBoolean("waxFullLock", false)
        doAxeBreak = config.getBoolean("doAxeBreak", true)
        invisibleKey = NamespacedKey(this, "invisibleframe")
        waxedKey = NamespacedKey(this, "waxedframe")

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

    fun setEnableWax(value: Boolean) {
        enableWax = value
        config.set("enableWax", value)
        saveConfig()
    }

    fun setWaxFullLock(value: Boolean) {
        waxFullLock = value
        config.set("waxFullLock", value)
        saveConfig()
    }

    fun setDoAxeBreak(value: Boolean) {
        doAxeBreak = value
        config.set("doAxeBreak", value)
        saveConfig()
    }
}
