package online.slavok.frames.plugin

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType

class FrameListener(private val plugin: SimpleFramesPlugin) : Listener {

    private fun isInvisible(frame: ItemFrame): Boolean =
        frame.persistentDataContainer.has(plugin.invisibleKey, PersistentDataType.BYTE)

    private fun tag(frame: ItemFrame) =
        frame.persistentDataContainer.set(plugin.invisibleKey, PersistentDataType.BYTE, 1.toByte())

    private fun untag(frame: ItemFrame) =
        frame.persistentDataContainer.remove(plugin.invisibleKey)

    /** A tagged frame is invisible only while it holds an item; empty -> visible. */
    private fun syncVisibility(frame: ItemFrame) {
        frame.setVisible(frame.item.type == Material.AIR)
    }

    private fun restore(frame: ItemFrame) {
        untag(frame)
        frame.setVisible(true)
        effects(frame, Sound.ENTITY_ITEM_FRAME_PLACE, Particle.CRIT, 10, 0.3)
    }

    // Left-click (attack): shears -> invisible, leather -> restore, and taking the
    // item out of an invisible frame makes it visible again.
    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val player = event.damager as? Player ?: return
        val hand = player.inventory.itemInMainHand

        if (hand.type == Material.SHEARS && !isInvisible(frame)) {
            event.isCancelled = true
            if (player.gameMode != GameMode.CREATIVE && plugin.doShearsBreak) damageShears(hand)
            tag(frame)
            syncVisibility(frame)
            effects(frame, Sound.ENTITY_SNOW_GOLEM_SHEAR, Particle.CLOUD, 3, 0.0)
            return
        }

        if (hand.type == Material.LEATHER && isInvisible(frame) && plugin.fixWithLeather) {
            event.isCancelled = true
            if (player.gameMode != GameMode.CREATIVE) hand.amount -= 1
            restore(frame)
            return
        }

        // Attacking an invisible frame that holds an item removes the item (vanilla),
        // leaving it empty -> it must become visible.
        if (isInvisible(frame) && frame.item.type != Material.AIR) {
            frame.setVisible(true)
        }
    }

    // Right-click: leather restores too (instead of being placed into the frame);
    // placing any other item into an invisible frame hides it again.
    @EventHandler(ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEntityEvent) {
        val frame = event.rightClicked as? ItemFrame ?: return
        if (!isInvisible(frame)) return
        val hand = event.player.inventory.itemInMainHand

        if (hand.type == Material.LEATHER && plugin.fixWithLeather) {
            event.isCancelled = true
            if (event.player.gameMode != GameMode.CREATIVE) hand.amount -= 1
            restore(frame)
            return
        }

        // Placing an item into an empty invisible frame hides it.
        if (frame.item.type == Material.AIR && hand.type != Material.AIR) {
            frame.setVisible(false)
        }
    }

    // Persist: a player breaking an invisible frame drops a tagged frame item so
    // placing it again keeps it invisible. Tied to fixWithLeather like the mod.
    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: HangingBreakByEntityEvent) {
        val frame = event.entity as? ItemFrame ?: return
        if (!plugin.fixWithLeather || !isInvisible(frame)) return
        val remover = event.remover
        if (remover !is Player || remover.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true
        val world = frame.world
        val loc = frame.location

        val content = frame.item
        if (content.type != Material.AIR) world.dropItemNaturally(loc, content)

        val frameMaterial = if (frame is GlowItemFrame) Material.GLOW_ITEM_FRAME else Material.ITEM_FRAME
        val drop = ItemStack(frameMaterial)
        val meta = drop.itemMeta
        if (meta != null) {
            meta.persistentDataContainer.set(plugin.invisibleKey, PersistentDataType.BYTE, 1.toByte())
            drop.itemMeta = meta
        }
        world.dropItemNaturally(loc, drop)

        frame.remove()
    }

    // Placing a tagged frame item -> the new (empty) frame is tagged; visible until an
    // item is put in it.
    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: HangingPlaceEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val item = event.itemStack ?: return
        val meta = item.itemMeta ?: return
        if (!meta.persistentDataContainer.has(plugin.invisibleKey, PersistentDataType.BYTE)) return
        tag(frame)
        syncVisibility(frame)
    }

    private fun damageShears(item: ItemStack) {
        val meta = item.itemMeta
        if (meta is Damageable) {
            val next = meta.damage + 1
            if (next >= item.type.maxDurability) {
                item.amount -= 1
            } else {
                meta.damage = next
                item.itemMeta = meta
            }
        }
    }

    private fun effects(frame: ItemFrame, sound: Sound, particle: Particle, count: Int, spread: Double) {
        val loc = frame.location
        frame.world.playSound(loc, sound, 1f, 1.5f)
        frame.world.spawnParticle(particle, loc, count, spread, spread, spread, 0.1)
    }
}
