package online.slavok.frames.plugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
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

    /**
     * Left-click (attack) with shears/leather. Works for both events: a frame that
     * holds an item fires EntityDamageByEntityEvent, an empty frame fires
     * HangingBreakByEntityEvent — either way this makes it (in)visible instead of
     * dropping/breaking. Returns true (and cancels via [cancel]) when it acted.
     */
    private fun tryTool(frame: ItemFrame, player: Player, cancel: () -> Unit): Boolean {
        val hand = player.inventory.itemInMainHand
        if (hand.type == Material.SHEARS && !isInvisible(frame)) {
            cancel()
            if (player.gameMode != GameMode.CREATIVE && plugin.doShearsBreak) damageShears(hand)
            tag(frame)
            syncVisibility(frame)
            effects(frame, Sound.ENTITY_SNOW_GOLEM_SHEAR, Particle.CLOUD, 3, 0.0)
            return true
        }
        if (hand.type == Material.LEATHER && isInvisible(frame) && plugin.fixWithLeather) {
            cancel()
            if (player.gameMode != GameMode.CREATIVE) hand.amount -= 1
            restore(frame)
            return true
        }
        return false
    }

    // Attacking a frame that HOLDS an item: shears/leather, or taking the item out
    // (which empties it, so it must become visible).
    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val player = event.damager as? Player ?: return
        if (tryTool(frame, player) { event.isCancelled = true }) return

        if (isInvisible(frame) && frame.item.type != Material.AIR) {
            frame.setVisible(true) // the item is about to be removed -> visible
        }
    }

    // Attacking/breaking an EMPTY frame: shears/leather act here (an empty frame
    // breaks instead of firing the damage event). Otherwise persist an invisible
    // frame as a tagged dropped item.
    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: HangingBreakByEntityEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val player = event.remover as? Player ?: return
        if (tryTool(frame, player) { event.isCancelled = true }) return

        if (!plugin.fixWithLeather || !isInvisible(frame) || player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true
        val world = frame.world
        val loc = frame.location

        val content = frame.item
        if (content.type != Material.AIR) world.dropItemNaturally(loc, content)

        val glow = frame is GlowItemFrame
        val drop = ItemStack(if (glow) Material.GLOW_ITEM_FRAME else Material.ITEM_FRAME)
        val meta = drop.itemMeta
        if (meta != null) {
            meta.persistentDataContainer.set(plugin.invisibleKey, PersistentDataType.BYTE, 1.toByte())
            setFrameName(meta, if (glow) plugin.invisibleGlowFrameName else plugin.invisibleFrameName)
            // Glint without a visible enchant, version-stably (setEnchantmentGlintOverride
            // is 1.20.5+ only): a hidden dummy enchant, resolved by registry key so the
            // constant rename (DURABILITY -> UNBREAKING) doesn't matter across versions.
            val glint = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"))
            if (glint != null) {
                meta.addEnchant(glint, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }
            drop.itemMeta = meta
        }
        world.dropItemNaturally(loc, drop)

        frame.remove()
    }

    // Right-click places an item into the frame (vanilla); an item in an invisible
    // frame hides it. Restoring is left-click only (see tryTool), so leather can
    // still be placed into a frame normally.
    @EventHandler(ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEntityEvent) {
        val frame = event.rightClicked as? ItemFrame ?: return
        if (!isInvisible(frame)) return
        val hand = event.player.inventory.itemInMainHand
        if (frame.item.type == Material.AIR && hand.type != Material.AIR) {
            frame.setVisible(false)
        }
    }

    // Placing a tagged frame item -> the new (empty) frame is tagged; visible until
    // an item is put in it.
    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: HangingPlaceEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val item = event.itemStack ?: return
        val meta = item.itemMeta ?: return
        if (!meta.persistentDataContainer.has(plugin.invisibleKey, PersistentDataType.BYTE)) return
        tag(frame)
        syncVisibility(frame)
    }

    // Non-italic on every version: an Adventure component with the ITALIC decoration
    // explicitly disabled (custom names render italic only when italic is left unset).
    private fun setFrameName(meta: ItemMeta, name: String) {
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false))
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
