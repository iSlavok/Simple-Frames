package online.slavok.frames.plugin

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Pure listener logic via Mockito (no MockBukkit entity coverage needed). Players are
 * CREATIVE so the durability/consume paths (which need the server's ItemFactory) are
 * skipped — the tests focus on the visibility + tag decisions.
 */
class FrameListenerTest {
    private val key = NamespacedKey("simpleframes", "invisibleframe")

    private fun mockPlugin(): SimpleFramesPlugin {
        val p = mock<SimpleFramesPlugin>()
        whenever(p.invisibleKey).thenReturn(key)
        whenever(p.doShearsBreak).thenReturn(true)
        whenever(p.fixWithLeather).thenReturn(true)
        return p
    }

    private fun mockPlayer(handType: Material): Player {
        val hand = mock<ItemStack>()
        whenever(hand.type).thenReturn(handType)
        val inv = mock<PlayerInventory>()
        whenever(inv.itemInMainHand).thenReturn(hand)
        val player = mock<Player>()
        whenever(player.inventory).thenReturn(inv)
        whenever(player.gameMode).thenReturn(GameMode.CREATIVE)
        return player
    }

    private fun mockFrame(invisible: Boolean): Pair<ItemFrame, PersistentDataContainer> {
        val pdc = mock<PersistentDataContainer>()
        whenever(pdc.has(key, PersistentDataType.BYTE)).thenReturn(invisible)
        val frame = mock<ItemFrame>()
        whenever(frame.persistentDataContainer).thenReturn(pdc)
        whenever(frame.world).thenReturn(mock<World>())
        whenever(frame.location).thenReturn(mock<Location>())
        return frame to pdc
    }

    private fun damageEvent(frame: ItemFrame, player: Player): EntityDamageByEntityEvent {
        val event = mock<EntityDamageByEntityEvent>()
        whenever(event.entity).thenReturn(frame)
        whenever(event.damager).thenReturn(player)
        return event
    }

    @Test
    fun `shears make a normal frame invisible`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false)
        val event = damageEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onDamage(event)

        verify(event).isCancelled = true
        verify(frame).setVisible(false)
        verify(pdc).set(key, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `leather restores an invisible frame`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = true)
        val event = damageEvent(frame, mockPlayer(Material.LEATHER))

        FrameListener(plugin).onDamage(event)

        verify(event).isCancelled = true
        verify(frame).setVisible(true)
        verify(pdc).remove(key)
    }

    // Negative control: shears on an already-invisible frame do nothing.
    @Test
    fun `shears do nothing to an already-invisible frame`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = true)
        val event = damageEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onDamage(event)

        verify(frame, never()).setVisible(any())
    }

    // Negative control: leather does nothing to a normal (visible) frame.
    @Test
    fun `leather does nothing to a normal frame`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = false)
        val event = damageEvent(frame, mockPlayer(Material.LEATHER))

        FrameListener(plugin).onDamage(event)

        verify(frame, never()).setVisible(any())
    }
}
