package online.slavok.frames.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClickModeTest {
    @Test
    fun `allows flags`() {
        assertTrue(ClickMode.LEFT.allowsLeft()); assertFalse(ClickMode.LEFT.allowsRight())
        assertFalse(ClickMode.RIGHT.allowsLeft()); assertTrue(ClickMode.RIGHT.allowsRight())
        assertTrue(ClickMode.BOTH.allowsLeft()); assertTrue(ClickMode.BOTH.allowsRight())
    }

    @Test
    fun `parse is case-insensitive and trims`() {
        assertEquals(ClickMode.RIGHT, ClickMode.parse(" right ", ClickMode.LEFT))
        assertEquals(ClickMode.BOTH, ClickMode.parse("BOTH", ClickMode.LEFT))
    }

    @Test
    fun `parse falls back on null or garbage`() {
        assertEquals(ClickMode.LEFT, ClickMode.parse(null, ClickMode.LEFT))
        assertEquals(ClickMode.BOTH, ClickMode.parse("sideways", ClickMode.BOTH))
    }
}
