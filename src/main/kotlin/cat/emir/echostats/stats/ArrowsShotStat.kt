package cat.emir.echostats.stats

import cat.emir.echostats.item.ItemStat
import cat.emir.echostats.item.ItemStats
import cat.emir.echostats.item.ItemStats.Companion.pdcKey
import cat.emir.echostats.item.getTagContainer
import cat.emir.echostats.item.toComponent
import cat.emir.echostats.item.toFormattedString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

fun ItemStats.increaseArrowsShot() {
    item.editPersistentDataContainer { pdc ->
        val statsPDC = pdc.getTagContainer(pdcKey("stats"))
        val value = statsPDC.getOrDefault(pdcKey("arrows_shot"), PersistentDataType.LONG, 0)

        statsPDC.set(pdcKey("arrows_shot"), PersistentDataType.LONG, value + 1)
        pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
    }
}

class ArrowsShotStat(item: ItemStack) : ItemStat(item) {
    override fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int) {
        val value = pdc.getOrDefault(pdcKey("arrows_shot"), PersistentDataType.LONG, 0)
        if (value > 0) {
            lore.add("<gray>Arrows Shot: ${value.toFormattedString(player.locale())}".toComponent())
        }
    }
}