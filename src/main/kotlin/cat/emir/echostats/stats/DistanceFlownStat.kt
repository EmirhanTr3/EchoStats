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
import kotlin.math.roundToLong

fun ItemStats.increaseDistanceFlown(distance: Float) {
    item.editPersistentDataContainer { pdc ->
        val statsPDC = pdc.getTagContainer(pdcKey("stats"))
        val value = statsPDC.getOrDefault(pdcKey("distance_flown"), PersistentDataType.FLOAT, 0F)

        statsPDC.set(pdcKey("distance_flown"), PersistentDataType.FLOAT, value + distance)
        pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
    }
}

class DistanceFlownStat(item: ItemStack) : ItemStat(item) {
    override fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int) {
        val value = pdc.getOrDefault(pdcKey("distance_flown"), PersistentDataType.FLOAT, 0F)
        if (value > 0) {
            lore.add("<gray>Distance Flown: ${value.roundToLong().toFormattedString(player.locale())} blocks".toComponent())
        }
    }
}