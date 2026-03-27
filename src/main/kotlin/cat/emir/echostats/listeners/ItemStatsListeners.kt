package cat.emir.echostats.listeners

import cat.emir.echostats.EchoStats
import cat.emir.echostats.TagUtils
import cat.emir.echostats.item.ItemStats
import cat.emir.echostats.stats
import cat.emir.echostats.stats.TimesFishedStat
import cat.emir.echostats.stats.increaseArrowsShot
import cat.emir.echostats.stats.increaseAttacksBlocked
import cat.emir.echostats.stats.increaseBlocksBroken
import cat.emir.echostats.stats.increaseDistanceFlown
import cat.emir.echostats.stats.increaseKill
import cat.emir.echostats.stats.increaseMobsSheared
import cat.emir.echostats.stats.increaseTimesFished
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockShearEntityEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class ItemStatsListeners(val plugin: EchoStats) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        if (event.click != ClickType.SHIFT_RIGHT || !ItemStats.hasStats(item)) return
        item.stats.toggleMoreView()
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!TagUtils.isTool(event.player.inventory.itemInMainHand.type)) return
        event.player.inventory.itemInMainHand.stats.increaseBlocksBroken(event.block.type)
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
        val attacker = event.damageSource.causingEntity
        if (attacker !is LivingEntity || attacker.equipment == null || !TagUtils.isWeapon(attacker.equipment!!.itemInMainHand.type)) return
        attacker.equipment!!.itemInMainHand.stats.increaseKill(event.entity)
    }

    @EventHandler
    fun onPlayerShearEntity(event: PlayerShearEntityEvent) {
        val entity = event.entity
        if (entity !is LivingEntity || event.player.inventory.itemInMainHand.type != Material.SHEARS) return
        event.player.inventory.itemInMainHand.stats.increaseMobsSheared(entity)
    }

    @EventHandler
    fun onBlockShearEntity(event: BlockShearEntityEvent) {
        val entity = event.entity
        if (entity !is LivingEntity || event.block.type != Material.DISPENSER || event.tool.type != Material.SHEARS) return

        val state = (event.block.state as InventoryHolder)
        val item = state.inventory.find { it == event.tool }
        if (item == null) return

        item.stats.increaseMobsSheared(entity)
    }

    @EventHandler
    fun onShoot(event: EntityShootBowEvent) {
        val bow = event.bow ?: return
        bow.stats.increaseArrowsShot()
    }

    @EventHandler
    fun onShieldBlock(event: EntityDamageByEntityEvent) {
        val victim = event.entity
        val attacker = event.damager
        if (event.finalDamage != 0.0) return
        if (victim !is LivingEntity || attacker !is LivingEntity ||
            !victim.hasActiveItem() || victim.activeItem.type != Material.SHIELD) return

        victim.activeItem.stats.increaseAttacksBlocked(attacker)
    }

    @EventHandler
    fun onFish(event: PlayerFishEvent) {
        if (event.player.inventory.itemInMainHand.type != Material.FISHING_ROD
            || event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val entity = event.caught ?: return

        event.player.inventory.itemInMainHand.stats.increaseTimesFished(
            if (TagUtils.isFish((entity as Item).itemStack.type)) TimesFishedStat.Type.FISH else TimesFishedStat.Type.ITEM)
    }

    val elytraTrackedPlayers = mutableMapOf<UUID, BukkitTask>()
    val elytraLastLocation = mutableMapOf<UUID, Location>()

    @EventHandler
    fun onElytraChange(event: PlayerArmorChangeEvent) {
        if (event.slot != EquipmentSlot.CHEST
            || (event.oldItem.type == Material.ELYTRA && event.newItem.type == Material.ELYTRA)) return

        if (event.oldItem.type == Material.ELYTRA && event.newItem.type != Material.ELYTRA) {
            elytraTrackedPlayers[event.player.uniqueId]?.cancel()
            elytraTrackedPlayers.remove(event.player.uniqueId)
            elytraLastLocation.remove(event.player.uniqueId)

        } else if (event.oldItem.type != Material.ELYTRA && event.newItem.type == Material.ELYTRA
            && !elytraTrackedPlayers.containsKey(event.player.uniqueId)) {

            elytraLastLocation[event.player.uniqueId] = event.player.location
            elytraTrackedPlayers[event.player.uniqueId] = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
                if (event.player.inventory.chestplate?.type != Material.ELYTRA || !event.player.isGliding) return@Runnable

                elytraLastLocation[event.player.uniqueId]?.distance(event.player.location)?.let {
                    if (it > 0) event.player.inventory.chestplate!!.stats.increaseDistanceFlown(it.toFloat())
                }

                elytraLastLocation[event.player.uniqueId] = event.player.location
            }, 0, 20)

        }
    }
}