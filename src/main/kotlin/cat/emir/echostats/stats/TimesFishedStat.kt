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

fun ItemStats.increaseTimesFished(type: TimesFishedStat.Type) {
    item.editPersistentDataContainer { pdc ->
        val statsPDC = pdc.getTagContainer(pdcKey("stats"))
        val timesFishedPDC = statsPDC.getTagContainer(pdcKey("times_fished"))
        val fishAmount = timesFishedPDC.getOrDefault(pdcKey("fish"), PersistentDataType.LONG, 0)
        val itemAmount = timesFishedPDC.getOrDefault(pdcKey("item"), PersistentDataType.LONG, 0)

        if (type == TimesFishedStat.Type.FISH) {
            timesFishedPDC.set(pdcKey("fish"), PersistentDataType.LONG, fishAmount + 1)
        } else if (type == TimesFishedStat.Type.ITEM) {
            timesFishedPDC.set(pdcKey("item"), PersistentDataType.LONG, itemAmount + 1)
        }

        statsPDC.set(pdcKey("times_fished"), PersistentDataType.TAG_CONTAINER, timesFishedPDC)
        pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
    }
}

class TimesFishedStat(item: ItemStack) : ItemStat(item) {
    enum class Type {
        FISH,
        ITEM
    }

    override fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int) {
        val timesFishedPDC = pdc.get(pdcKey("times_fished"), PersistentDataType.TAG_CONTAINER)
        if (timesFishedPDC != null) {
            val fishAmount = timesFishedPDC.getOrDefault(pdcKey("fish"), PersistentDataType.LONG, 0)
            val itemAmount = timesFishedPDC.getOrDefault(pdcKey("item"), PersistentDataType.LONG, 0)

            val total = fishAmount + itemAmount

            lore.add("<gray>Times Fished: ${total.toFormattedString(player.locale())}".toComponent())
            if (fishAmount > 0)
                lore.add(" <dark_gray>-</dark_gray> <gray>Fish: ${fishAmount.toFormattedString(player.locale())}".toComponent())
            if (itemAmount > 0)
                lore.add(" <dark_gray>-</dark_gray> <gray>Item: ${itemAmount.toFormattedString(player.locale())}".toComponent())
        }
    }
}