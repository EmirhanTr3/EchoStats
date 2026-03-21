package cat.emir.echostats.item

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

abstract class ItemStat(val item: ItemStack) {
    abstract fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int)
}