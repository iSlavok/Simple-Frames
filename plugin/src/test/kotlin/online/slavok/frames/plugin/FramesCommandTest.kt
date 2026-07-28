package online.slavok.frames.plugin

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

/** Pure tab-completion logic for /simpleframes via Mockito. */
class FramesCommandTest {
    private val command = FramesCommand(mock<SimpleFramesPlugin>())
    private val sender = mock<CommandSender>()
    private val bukkitCommand = mock<Command>()

    private fun complete(vararg args: String): List<String> =
        command.onTabComplete(sender, bukkitCommand, "simpleframes", arrayOf(*args))

    @Test
    fun `first arg with prefix matches case-insensitively`() {
        assertEquals(listOf("enableWax"), complete("en"))
    }

    @Test
    fun `first arg with empty prefix returns all options`() {
        assertEquals(
            setOf("doShearsBreak", "doLeatherFix", "enableWax", "waxFullLock", "doAxeBreak"),
            complete("").toSet(),
        )
        assertEquals(5, complete("").size)
    }

    @Test
    fun `first arg with prefix do returns the do options`() {
        assertEquals(
            setOf("doShearsBreak", "doLeatherFix", "doAxeBreak"),
            complete("do").toSet(),
        )
        assertEquals(3, complete("do").size)
    }

    @Test
    fun `second arg with prefix t matches true`() {
        assertEquals(listOf("true"), complete("enableWax", "t"))
    }

    @Test
    fun `second arg with empty prefix returns true and false`() {
        assertEquals(setOf("true", "false"), complete("enableWax", "").toSet())
        assertEquals(2, complete("enableWax", "").size)
    }

    @Test
    fun `third arg and beyond returns nothing`() {
        assertEquals(emptyList<String>(), complete("enableWax", "true", "extra"))
    }
}
