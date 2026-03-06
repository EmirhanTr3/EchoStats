package cat.emir.echostats.listeners

import cat.emir.echostats.EchoStats
import cat.emir.echostats.item.ItemStats
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class ItemStatsListeners(val plugin: EchoStats) : Listener {
    private fun isTool(type: Material) = Tag.ITEMS_ENCHANTABLE_MINING.isTagged(type)
    private fun isWeapon(type: Material) = Tag.ITEMS_ENCHANTABLE_WEAPON.isTagged(type)
    private fun isArmor(type: Material) = Tag.ITEMS_ENCHANTABLE_ARMOR.isTagged(type)

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!isTool(event.player.inventory.itemInMainHand.type)) return
        ItemStats(event.player.inventory.itemInMainHand).increaseBlocksBroken(event.block.type)
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
        val attacker = event.damageSource.causingEntity
        if (attacker !is LivingEntity || attacker.equipment == null || !isWeapon(attacker.equipment!!.itemInMainHand.type)) return
        ItemStats(attacker.equipment!!.itemInMainHand).increaseKill(event.entity)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        if (event.click != ClickType.SHIFT_RIGHT) return
        if (!ItemStats.hasStats(item)) return
        ItemStats(item).toggleMoreView()
        event.isCancelled = true
    }
}