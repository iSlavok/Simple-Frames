package online.slavok.frames.plugin

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
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
    private val waxKey = NamespacedKey("simpleframes", "waxedframe")

    private fun mockPlugin(): SimpleFramesPlugin {
        val p = mock<SimpleFramesPlugin>()
        whenever(p.invisibleKey).thenReturn(key)
        whenever(p.waxedKey).thenReturn(waxKey)
        whenever(p.doShearsBreak).thenReturn(true)
        whenever(p.fixWithLeather).thenReturn(true)
        whenever(p.enableWax).thenReturn(true)
        whenever(p.waxFullLock).thenReturn(false)
        whenever(p.doAxeBreak).thenReturn(true)
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

    private fun mockFrame(
        invisible: Boolean,
        itemType: Material,
        waxed: Boolean = false,
    ): Pair<ItemFrame, PersistentDataContainer> {
        val pdc = mock<PersistentDataContainer>()
        whenever(pdc.has(key, PersistentDataType.BYTE)).thenReturn(invisible)
        whenever(pdc.has(waxKey, PersistentDataType.BYTE)).thenReturn(waxed)
        val held = mock<ItemStack>()
        whenever(held.type).thenReturn(itemType)
        val frame = mock<ItemFrame>()
        whenever(frame.persistentDataContainer).thenReturn(pdc)
        whenever(frame.item).thenReturn(held)
        whenever(frame.world).thenReturn(mock<World>())
        whenever(frame.location).thenReturn(mock<Location>())
        return frame to pdc
    }

    private fun interactEvent(frame: ItemFrame, player: Player): PlayerInteractEntityEvent {
        val event = mock<PlayerInteractEntityEvent>()
        whenever(event.rightClicked).thenReturn(frame)
        whenever(event.player).thenReturn(player)
        return event
    }

    private fun damageEvent(frame: ItemFrame, player: Player): EntityDamageByEntityEvent {
        val event = mock<EntityDamageByEntityEvent>()
        whenever(event.entity).thenReturn(frame)
        whenever(event.damager).thenReturn(player)
        return event
    }

    private fun breakEvent(frame: ItemFrame, player: Player): HangingBreakByEntityEvent {
        val event = mock<HangingBreakByEntityEvent>()
        whenever(event.entity).thenReturn(frame)
        whenever(event.remover).thenReturn(player)
        return event
    }

    // Empty frame: attacking fires the break event, not the damage event.
    @Test
    fun `shears on an empty frame make it invisible instead of breaking it`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = breakEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onBreak(event)

        verify(event).isCancelled = true // not broken
        verify(pdc).set(key, PersistentDataType.BYTE, 1.toByte())
        verify(frame).setVisible(true) // empty -> visible
    }

    @Test
    fun `leather on an empty invisible frame restores it instead of breaking it`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = breakEvent(frame, mockPlayer(Material.LEATHER))

        FrameListener(plugin).onBreak(event)

        verify(event).isCancelled = true
        verify(pdc).remove(key)
        verify(frame).setVisible(true)
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

    // Right-click places an item into an empty invisible frame -> it hides again.
    @Test
    fun `placing an item in an empty invisible frame hides it`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = true, itemType = Material.AIR)
        val player = mockPlayer(Material.DIAMOND)
        val event = mock<PlayerInteractEntityEvent>()
        whenever(event.rightClicked).thenReturn(frame)
        whenever(event.player).thenReturn(player)

        FrameListener(plugin).onInteract(event)

        verify(frame).setVisible(false)
    }

    // Restore is left-click only: right-clicking leather just puts it into the frame.
    @Test
    fun `right-click leather does not restore`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val player = mockPlayer(Material.LEATHER)
        val event = mock<PlayerInteractEntityEvent>()
        whenever(event.rightClicked).thenReturn(frame)
        whenever(event.player).thenReturn(player)

        FrameListener(plugin).onInteract(event)

        verify(pdc, never()).remove(key) // not restored
        verify(frame).setVisible(false)  // leather placed -> hidden
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

    // --- Wax feature ---

    @Test
    fun `honeycomb waxes a frame holding an item`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val event = interactEvent(frame, mockPlayer(Material.HONEYCOMB))

        FrameListener(plugin).onInteract(event)

        verify(pdc).set(waxKey, PersistentDataType.BYTE, 1.toByte())
        verify(event).isCancelled = true
    }

    @Test
    fun `honeycomb does not wax an empty frame`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.HONEYCOMB))

        FrameListener(plugin).onInteract(event)

        verify(pdc, never()).set(waxKey, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `axe removes wax from a waxed frame`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = interactEvent(frame, mockPlayer(Material.DIAMOND_AXE))

        FrameListener(plugin).onInteract(event)

        verify(pdc).remove(waxKey)
        verify(event).isCancelled = true
    }

    @Test
    fun `waxed frame blocks a plain right-click (rotation)`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = interactEvent(frame, mockPlayer(Material.AIR))

        FrameListener(plugin).onInteract(event)

        verify(event).isCancelled = true
    }

    @Test
    fun `wax handling is skipped when the feature is disabled`() {
        val plugin = mockPlugin()
        whenever(plugin.enableWax).thenReturn(false)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val event = interactEvent(frame, mockPlayer(Material.HONEYCOMB))

        FrameListener(plugin).onInteract(event)

        verify(pdc, never()).set(waxKey, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `full lock cancels damage to a waxed frame`() {
        val plugin = mockPlugin()
        whenever(plugin.waxFullLock).thenReturn(true)
        val (frame, _) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = damageEvent(frame, mockPlayer(Material.AIR))

        FrameListener(plugin).onDamageFullLock(event)

        verify(event).isCancelled = true
    }

    @Test
    fun `full lock does nothing at var2`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = damageEvent(frame, mockPlayer(Material.AIR))

        FrameListener(plugin).onDamageFullLock(event)

        verify(event, never()).isCancelled = true
    }

    // var2: knocking the item out of a waxed frame auto-removes the wax so the frame
    // can be reused (an empty waxed frame would otherwise reject a new item).
    @Test
    fun `knocking the item out of a waxed frame removes the wax`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = damageEvent(frame, mockPlayer(Material.AIR))

        FrameListener(plugin).onDamage(event)

        verify(pdc).remove(waxKey)
    }

    // Shearing a waxed frame makes it invisible but keeps its item -> wax stays.
    @Test
    fun `shears on a waxed frame keep the wax`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = damageEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onDamage(event)

        verify(pdc, never()).remove(waxKey)
        verify(pdc).set(key, PersistentDataType.BYTE, 1.toByte())
    }
}
