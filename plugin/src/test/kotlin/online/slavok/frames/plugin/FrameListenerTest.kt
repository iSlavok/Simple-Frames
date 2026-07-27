package online.slavok.frames.plugin

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
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
 * Pure listener logic via Mockito. Players are CREATIVE so the durability/consume
 * paths (which need the server ItemFactory) are skipped; the tests focus on the
 * visibility + tag decisions. Key invariant: a tagged frame is invisible only while
 * it holds an item (empty -> visible).
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

    private fun mockFrame(invisible: Boolean, itemType: Material): Pair<ItemFrame, PersistentDataContainer> {
        val pdc = mock<PersistentDataContainer>()
        whenever(pdc.has(key, PersistentDataType.BYTE)).thenReturn(invisible)
        val held = mock<ItemStack>()
        whenever(held.type).thenReturn(itemType)
        val frame = mock<ItemFrame>()
        whenever(frame.persistentDataContainer).thenReturn(pdc)
        whenever(frame.item).thenReturn(held)
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
    fun `shears on a frame holding an item make it invisible`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val event = damageEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onDamage(event)

        verify(event).isCancelled = true
        verify(pdc).set(key, PersistentDataType.BYTE, 1.toByte())
        verify(frame).setVisible(false) // holds an item -> invisible
    }

    @Test
    fun `shears on an empty frame tag it but keep it visible`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = damageEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onDamage(event)

        verify(pdc).set(key, PersistentDataType.BYTE, 1.toByte())
        verify(frame).setVisible(true) // empty -> visible
    }

    @Test
    fun `taking the item out of an invisible frame makes it visible`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = true, itemType = Material.DIAMOND)
        val event = damageEvent(frame, mockPlayer(Material.AIR)) // empty-hand attack removes the item

        FrameListener(plugin).onDamage(event)

        verify(frame).setVisible(true)
    }

    @Test
    fun `leather restores an invisible frame (attack)`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = damageEvent(frame, mockPlayer(Material.LEATHER))

        FrameListener(plugin).onDamage(event)

        verify(event).isCancelled = true
        verify(frame).setVisible(true)
        verify(pdc).remove(key)
    }

    @Test
    fun `leather restores an invisible frame (right-click)`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val player = mockPlayer(Material.LEATHER)
        val event = mock<PlayerInteractEntityEvent>()
        whenever(event.rightClicked).thenReturn(frame)
        whenever(event.player).thenReturn(player)

        FrameListener(plugin).onInteract(event)

        verify(event).isCancelled = true
        verify(frame).setVisible(true)
        verify(pdc).remove(key)
    }

    // Negative control: shears on an already-invisible EMPTY frame do nothing.
    @Test
    fun `shears do nothing to an already-invisible empty frame`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = damageEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onDamage(event)

        verify(frame, never()).setVisible(any())
    }

    // Negative control: leather does nothing to a normal (untagged) frame.
    @Test
    fun `leather does nothing to a normal frame`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = damageEvent(frame, mockPlayer(Material.LEATHER))

        FrameListener(plugin).onDamage(event)

        verify(frame, never()).setVisible(any())
    }
}
