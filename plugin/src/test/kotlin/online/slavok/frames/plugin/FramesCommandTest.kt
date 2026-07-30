package online.slavok.frames.plugin

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/** Pure command + tab-completion logic for /simpleframes via Mockito. */
class FramesCommandTest {
    private val plugin = mock<SimpleFramesPlugin>()
    private val command = FramesCommand(plugin)
    private val sender = mock<CommandSender>()
    private val bukkitCommand = mock<Command>()

    private fun complete(vararg args: String): List<String> =
        command.onTabComplete(sender, bukkitCommand, "simpleframes", arrayOf(*args))

    private fun run(vararg args: String): Boolean =
        command.onCommand(sender, bukkitCommand, "simpleframes", arrayOf(*args))

    @Test
    fun `first arg with prefix matches case-insensitively`() {
        assertEquals(listOf("enableWax"), complete("en"))
    }

    @Test
    fun `first arg with empty prefix returns all options`() {
        assertEquals(
            setOf(
                "doShearsBreak", "doLeatherFix", "enableWax", "waxFullLock", "doAxeBreak",
                "doLeatherConsume", "doHoneycombConsume",
                "shearsButton", "leatherButton", "honeycombButton", "axeButton",
            ),
            complete("").toSet(),
        )
        assertEquals(11, complete("").size)
    }

    @Test
    fun `first arg with prefix do returns the do options`() {
        assertEquals(
            setOf("doShearsBreak", "doLeatherFix", "doAxeBreak", "doLeatherConsume", "doHoneycombConsume"),
            complete("do").toSet(),
        )
    }

    @Test
    fun `first arg with prefix button matches the enum options`() {
        assertEquals(
            setOf("shearsButton", "leatherButton", "honeycombButton", "axeButton"),
            complete("").filter { it.endsWith("Button") }.toSet(),
        )
    }

    @Test
    fun `second arg for a boolean option returns true and false`() {
        assertEquals(setOf("true", "false"), complete("enableWax", "").toSet())
        assertEquals(2, complete("enableWax", "").size)
    }

    @Test
    fun `second arg for an enum option returns the click modes`() {
        assertEquals(listOf("BOTH", "LEFT", "RIGHT"), complete("shearsButton", ""))
    }

    @Test
    fun `second arg for a consume option returns true and false`() {
        assertEquals(setOf("true", "false"), complete("doLeatherConsume", "").toSet())
    }

    @Test
    fun `third arg and beyond returns nothing`() {
        assertEquals(emptyList<String>(), complete("enableWax", "true", "extra"))
    }

    @Test
    fun `setting an enum option parses and stores it`() {
        assertTrue(run("shearsButton", "right"))
        verify(plugin).setShearsButton(ClickMode.RIGHT)
    }

    @Test
    fun `an invalid enum value is rejected`() {
        assertTrue(run("shearsButton", "sideways"))
        verify(plugin, never()).setShearsButton(any())
    }

    @Test
    fun `setting a consume toggle stores it`() {
        assertTrue(run("doHoneycombConsume", "false"))
        verify(plugin).setDoHoneycombConsume(false)
    }
}
