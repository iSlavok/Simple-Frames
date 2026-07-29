package online.slavok.frames.plugin

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
        whenever(p.shearsButton).thenReturn(ClickMode.LEFT)
        whenever(p.leatherButton).thenReturn(ClickMode.LEFT)
        whenever(p.honeycombButton).thenReturn(ClickMode.BOTH)
        whenever(p.axeButton).thenReturn(ClickMode.BOTH)
        whenever(p.doLeatherConsume).thenReturn(true)
        whenever(p.doHoneycombConsume).thenReturn(true)
        whenever(p.invisibleFrameName).thenReturn("Invisible Item Frame")
        whenever(p.invisibleGlowFrameName).thenReturn("Invisible Glow Item Frame")
        return p
    }

    private fun mockPlayer(
        handType: Material,
        gameMode: GameMode = GameMode.CREATIVE,
        sneaking: Boolean = false,
    ): Player {
        val hand = mock<ItemStack>()
        whenever(hand.type).thenReturn(handType)
        val inv = mock<PlayerInventory>()
        whenever(inv.itemInMainHand).thenReturn(hand)
        val player = mock<Player>()
        whenever(player.inventory).thenReturn(inv)
        whenever(player.gameMode).thenReturn(gameMode)
        whenever(player.isSneaking).thenReturn(sneaking)
        whenever(player.hasPermission(any<String>())).thenReturn(true)
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

    private fun interactEvent(
        frame: ItemFrame,
        player: Player,
        hand: EquipmentSlot = EquipmentSlot.HAND,
    ): PlayerInteractEntityEvent {
        val event = mock<PlayerInteractEntityEvent>()
        whenever(event.rightClicked).thenReturn(frame)
        whenever(event.player).thenReturn(player)
        whenever(event.hand).thenReturn(hand)
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
        val event = interactEvent(frame, mockPlayer(Material.DIAMOND))

        FrameListener(plugin).onInteract(event)

        verify(frame).setVisible(false)
    }

    // Restore is left-click only by default: right-clicking leather just puts it into the frame.
    @Test
    fun `right-click leather does not restore`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.LEATHER))

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

    // --- Configurable buttons: right-click shears/leather + consume toggles ---

    @Test
    fun `right-click shears (RIGHT mode) make an empty frame invisible`() {
        val plugin = mockPlugin()
        whenever(plugin.shearsButton).thenReturn(ClickMode.RIGHT)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onInteract(event)

        verify(event).isCancelled = true
        verify(pdc).set(key, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `sneak right-click shears does not act (vanilla place)`() {
        val plugin = mockPlugin()
        whenever(plugin.shearsButton).thenReturn(ClickMode.RIGHT)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.SHEARS, sneaking = true))

        FrameListener(plugin).onInteract(event)

        verify(event, never()).isCancelled = true
        verify(pdc, never()).set(key, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `right-click leather (RIGHT mode) restores an invisible frame`() {
        val plugin = mockPlugin()
        whenever(plugin.leatherButton).thenReturn(ClickMode.RIGHT)
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.LEATHER))

        FrameListener(plugin).onInteract(event)

        verify(event).isCancelled = true
        verify(pdc).remove(key)
    }

    @Test
    fun `sneak right-click leather does not restore (vanilla place)`() {
        val plugin = mockPlugin()
        whenever(plugin.leatherButton).thenReturn(ClickMode.RIGHT)
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.LEATHER, sneaking = true))

        FrameListener(plugin).onInteract(event)

        verify(pdc, never()).remove(key)
    }

    @Test
    fun `right-click shears on an already-invisible frame falls through to placement`() {
        val plugin = mockPlugin()
        whenever(plugin.shearsButton).thenReturn(ClickMode.RIGHT)
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.SHEARS))

        FrameListener(plugin).onInteract(event)

        verify(event, never()).isCancelled = true // not cancelled -> vanilla can place the shears
        verify(pdc, never()).set(key, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `right-click leather on a visible frame falls through to placement`() {
        val plugin = mockPlugin()
        whenever(plugin.leatherButton).thenReturn(ClickMode.RIGHT)
        val (frame, _) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.LEATHER))

        FrameListener(plugin).onInteract(event)

        verify(event, never()).isCancelled = true // not cancelled -> vanilla can place the leather
    }

    // The off-hand copy of a make-invisible right-click must not hide the just-tagged empty
    // frame (an empty invisible frame stays visible until an item is placed).
    @Test
    fun `off-hand right-click shears does not hide a just-made-invisible empty frame`() {
        val plugin = mockPlugin()
        whenever(plugin.shearsButton).thenReturn(ClickMode.RIGHT)
        val (frame, _) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = interactEvent(frame, mockPlayer(Material.SHEARS), hand = EquipmentSlot.OFF_HAND)

        FrameListener(plugin).onInteract(event)

        verify(frame, never()).setVisible(false)
    }

    @Test
    fun `right-click honeycomb does nothing when honeycombButton is LEFT`() {
        val plugin = mockPlugin()
        whenever(plugin.honeycombButton).thenReturn(ClickMode.LEFT)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val event = interactEvent(frame, mockPlayer(Material.HONEYCOMB))

        FrameListener(plugin).onInteract(event)

        verify(pdc, never()).set(waxKey, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `honeycomb is not consumed when doHoneycombConsume is false`() {
        val plugin = mockPlugin()
        whenever(plugin.doHoneycombConsume).thenReturn(false)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val player = mockPlayer(Material.HONEYCOMB, gameMode = GameMode.SURVIVAL)
        val hand = player.inventory.itemInMainHand
        val event = interactEvent(frame, player)

        FrameListener(plugin).onInteract(event)

        verify(pdc).set(waxKey, PersistentDataType.BYTE, 1.toByte())
        verify(hand, never()).amount = any()
    }

    @Test
    fun `leather is not consumed when doLeatherConsume is false`() {
        val plugin = mockPlugin()
        whenever(plugin.leatherButton).thenReturn(ClickMode.RIGHT)
        whenever(plugin.doLeatherConsume).thenReturn(false)
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.AIR)
        val player = mockPlayer(Material.LEATHER, gameMode = GameMode.SURVIVAL)
        val hand = player.inventory.itemInMainHand
        val event = interactEvent(frame, player)

        FrameListener(plugin).onInteract(event)

        verify(pdc).remove(key)
        verify(hand, never()).amount = any()
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

    // The off-hand copy of the right-click still blocks, but must NOT replay the denied
    // click (the event fires once per hand; only the main hand plays the sound).
    @Test
    fun `off-hand right-click on a waxed frame blocks without a second denied sound`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = interactEvent(frame, mockPlayer(Material.AIR), hand = EquipmentSlot.OFF_HAND)

        FrameListener(plugin).onInteract(event)

        verify(event).isCancelled = true // still blocked
        verify(frame.world, never()).playSound(any<Location>(), any<Sound>(), any<SoundCategory>(), any<Float>(), any<Float>())
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

    // --- Configurable buttons: left-click honeycomb + left-click axe under full lock ---

    @Test
    fun `left-click honeycomb waxes a frame holding an item`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val event = damageEvent(frame, mockPlayer(Material.HONEYCOMB))

        FrameListener(plugin).onDamage(event)

        verify(pdc).set(waxKey, PersistentDataType.BYTE, 1.toByte())
        verify(event).isCancelled = true
    }

    @Test
    fun `left-click honeycomb does nothing when honeycombButton is RIGHT`() {
        val plugin = mockPlugin()
        whenever(plugin.honeycombButton).thenReturn(ClickMode.RIGHT)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val event = damageEvent(frame, mockPlayer(Material.HONEYCOMB))

        FrameListener(plugin).onDamage(event)

        verify(pdc, never()).set(waxKey, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `left-click axe under full lock removes the wax`() {
        val plugin = mockPlugin()
        whenever(plugin.waxFullLock).thenReturn(true)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = damageEvent(frame, mockPlayer(Material.DIAMOND_AXE))

        FrameListener(plugin).onDamageFullLock(event)

        verify(event).isCancelled = true
        verify(pdc).remove(waxKey)
    }

    @Test
    fun `left-click axe under full lock is denied when axeButton is RIGHT`() {
        val plugin = mockPlugin()
        whenever(plugin.waxFullLock).thenReturn(true)
        whenever(plugin.axeButton).thenReturn(ClickMode.RIGHT)
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = damageEvent(frame, mockPlayer(Material.DIAMOND_AXE))

        FrameListener(plugin).onDamageFullLock(event)

        verify(event).isCancelled = true
        verify(pdc, never()).remove(waxKey)
    }

    // --- Permission nodes: a missing node skips the action (no cancel) ---

    @Test
    fun `no shear permission -- shears do nothing`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val player = mockPlayer(Material.SHEARS)
        whenever(player.hasPermission("simpleframes.use.shear")).thenReturn(false)
        val event = damageEvent(frame, player)

        FrameListener(plugin).onDamage(event)

        verify(pdc, never()).set(key, PersistentDataType.BYTE, 1.toByte())
        verify(event, never()).isCancelled = true
    }

    @Test
    fun `no restore permission -- leather does not restore`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = true, itemType = Material.DIAMOND)
        val player = mockPlayer(Material.LEATHER)
        whenever(player.hasPermission("simpleframes.use.restore")).thenReturn(false)
        val event = damageEvent(frame, player)

        FrameListener(plugin).onDamage(event)

        verify(pdc, never()).remove(key)
    }

    @Test
    fun `no wax permission -- honeycomb does not wax`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND)
        val player = mockPlayer(Material.HONEYCOMB)
        whenever(player.hasPermission("simpleframes.use.wax")).thenReturn(false)
        val event = interactEvent(frame, player)

        FrameListener(plugin).onInteract(event)

        verify(pdc, never()).set(waxKey, PersistentDataType.BYTE, 1.toByte())
    }

    @Test
    fun `no unwax permission -- axe does not remove wax`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val player = mockPlayer(Material.DIAMOND_AXE)
        whenever(player.hasPermission("simpleframes.use.unwax")).thenReturn(false)
        val event = interactEvent(frame, player)

        FrameListener(plugin).onInteract(event)

        verify(pdc, never()).remove(waxKey)
    }

    private fun projectileDamageEvent(frame: ItemFrame, shooter: Any?): EntityDamageByEntityEvent {
        val arrow = mock<Projectile>()
        whenever(arrow.shooter).thenReturn(shooter as? org.bukkit.projectiles.ProjectileSource)
        val event = mock<EntityDamageByEntityEvent>()
        whenever(event.entity).thenReturn(frame)
        whenever(event.damager).thenReturn(arrow)
        return event
    }

    // A player's arrow that knocks the item out of a waxed frame also removes the wax.
    @Test
    fun `a player's projectile knocking the item out removes the wax`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = projectileDamageEvent(frame, mockPlayer(Material.AIR))

        FrameListener(plugin).onDamage(event)

        verify(pdc).remove(waxKey)
    }

    // A mob's arrow does not (matches Fabric: only a player attacker unwaxes).
    @Test
    fun `a mob projectile does not remove the wax`() {
        val plugin = mockPlugin()
        val (frame, pdc) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = projectileDamageEvent(frame, mock<LivingEntity>())

        FrameListener(plugin).onDamage(event)

        verify(pdc, never()).remove(waxKey)
    }

    // var3 no longer protects against support-block removal (PHYSICS) -> frame falls,
    // matching Fabric (which only blocks damage, not the attach/survival path).
    @Test
    fun `full lock does not cancel a PHYSICS break`() {
        val plugin = mockPlugin()
        whenever(plugin.waxFullLock).thenReturn(true)
        val event = mock<HangingBreakEvent>()
        whenever(event.cause).thenReturn(HangingBreakEvent.RemoveCause.PHYSICS)

        FrameListener(plugin).onBreakFullLock(event)

        verify(event, never()).isCancelled = true
    }

    // var3 still cancels a non-physics break (e.g. explosion) of a waxed frame.
    @Test
    fun `full lock cancels a non-physics break of a waxed frame`() {
        val plugin = mockPlugin()
        whenever(plugin.waxFullLock).thenReturn(true)
        val (frame, _) = mockFrame(invisible = false, itemType = Material.DIAMOND, waxed = true)
        val event = mock<HangingBreakEvent>()
        whenever(event.cause).thenReturn(HangingBreakEvent.RemoveCause.EXPLOSION)
        whenever(event.entity).thenReturn(frame)

        FrameListener(plugin).onBreakFullLock(event)

        verify(event).isCancelled = true
    }

    // --- onBreakPhysics: support-block removal (bug fix) ---

    // The bug: removing the block a frame is attached to fires HangingBreakEvent with
    // cause PHYSICS, which is NOT a HangingBreakByEntityEvent, so onBreak() never sees
    // it and the invisibility tag was lost. onBreakPhysics() must catch this case.
    @Test
    fun `PHYSICS break of a non-invisible frame is never cancelled`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = false, itemType = Material.AIR)
        val event = mock<HangingBreakEvent>()
        whenever(event.entity).thenReturn(frame)
        whenever(event.cause).thenReturn(HangingBreakEvent.RemoveCause.PHYSICS)

        FrameListener(plugin).onBreakPhysics(event)

        verify(event, never()).isCancelled = true
    }

    // HangingBreakByEntityEvent shares HangingBreakEvent's HandlerList, so this handler
    // also receives entity/player breaks -> it must ignore them and let onBreak() handle
    // it instead, or the two handlers would double-process the same break.
    @Test
    fun `onBreakPhysics ignores HangingBreakByEntityEvent (delegates to onBreak)`() {
        val plugin = mockPlugin()
        val (frame, _) = mockFrame(invisible = true, itemType = Material.AIR)
        val event = mock<HangingBreakByEntityEvent>()
        whenever(event.entity).thenReturn(frame)
        whenever(event.cause).thenReturn(HangingBreakEvent.RemoveCause.PHYSICS)

        FrameListener(plugin).onBreakPhysics(event)

        verify(event, never()).isCancelled = true
    }

    // The actual bug repro: a PHYSICS break of an invisible frame must be cancelled and
    // must drop a tagged, invisible frame item instead of letting vanilla drop a plain
    // one. Building/tagging that ItemStack needs a live Bukkit.getItemFactory(), so this
    // one test runs under MockBukkit (the frame/event themselves stay plain Mockito mocks,
    // matching the rest of this file's style).
    @Test
    fun `PHYSICS break of an invisible frame cancels and drops a tagged invisible frame item`() {
        MockBukkit.mock()
        try {
            val plugin = mockPlugin()
            val (frame, _) = mockFrame(invisible = true, itemType = Material.AIR)
            val event = mock<HangingBreakEvent>()
            whenever(event.entity).thenReturn(frame)
            whenever(event.cause).thenReturn(HangingBreakEvent.RemoveCause.PHYSICS)

            FrameListener(plugin).onBreakPhysics(event)

            verify(event).isCancelled = true
            verify(frame).remove()

            val dropCaptor = argumentCaptor<ItemStack>()
            verify(frame.world).dropItemNaturally(any<Location>(), dropCaptor.capture())
            val dropped = dropCaptor.firstValue
            assertTrue(dropped.itemMeta!!.persistentDataContainer.has(key, PersistentDataType.BYTE))
        } finally {
            MockBukkit.unmock()
        }
    }

    // --- Unbreaking / durability consumption ---
    // Vanilla non-armor tool rule: a durability point applies with probability
    // 1/(level+1). Level 0 preserves the old always-consume behaviour.

    @Test
    fun `level-0 unbreaking always consumes durability (always damages)`() {
        val listener = FrameListener(mockPlugin())
        val r = java.util.Random(42)
        repeat(10_000) {
            assertTrue(listener.consumesDurability(0, r), "level 0 must always damage")
        }
    }

    @Test
    fun `a negative unbreaking level always consumes durability`() {
        val listener = FrameListener(mockPlugin())
        val r = java.util.Random(7)
        repeat(1_000) {
            assertTrue(listener.consumesDurability(-1, r), "guarded level must always damage")
        }
    }

    // Unbreaking III consumes durability ~1/4 of the time. Assert a tolerance band
    // (not an exact count) so the statistical test stays stable.
    @Test
    fun `unbreaking level 3 consumes durability about a quarter of the time`() {
        val listener = FrameListener(mockPlugin())
        val r = java.util.Random(12345)
        val iterations = 100_000
        var consumed = 0
        repeat(iterations) {
            if (listener.consumesDurability(3, r)) consumed++
        }
        val ratio = consumed.toDouble() / iterations
        assertTrue(ratio in 0.24..0.26, "expected ~0.25 for Unbreaking III, got $ratio")
    }
}
