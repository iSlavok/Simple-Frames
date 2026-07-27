package online.slavok.frames.plugin

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

/** Load smoke test: a real (mock) server enables the plugin and registers its command. */
class SimpleFramesPluginTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: SimpleFramesPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(SimpleFramesPlugin::class.java)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `plugin enables`() {
        assertTrue(plugin.isEnabled)
    }

    @Test
    fun `command is registered`() {
        assertNotNull(plugin.getCommand("simpleframes"))
    }

    @Test
    fun `config defaults load`() {
        assertTrue(plugin.doShearsBreak)
        assertTrue(plugin.fixWithLeather)
    }
}
