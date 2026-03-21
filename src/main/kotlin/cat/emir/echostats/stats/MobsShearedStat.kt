package cat.emir.echostats.stats

import cat.emir.echostats.item.ItemStat
import cat.emir.echostats.item.ItemStats
import cat.emir.echostats.item.ItemStats.Companion.pdcKey
import cat.emir.echostats.item.getTagContainer
import cat.emir.echostats.item.toComponent
import cat.emir.echostats.item.toFormattedString
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.component1
import kotlin.collections.component2

fun ItemStats.increaseMobsSheared(entity: LivingEntity) {
    item.editPersistentDataContainer { pdc ->
        val statsPDC = pdc.getTagContainer(pdcKey("stats"))
        val mobsShearedPDC = statsPDC.getTagContainer(pdcKey("mobs_sheared"))
        val value = mobsShearedPDC.getOrDefault(entity.type.key, PersistentDataType.LONG, 0)

        mobsShearedPDC.set(entity.type.key, PersistentDataType.LONG, value + 1)
        statsPDC.set(pdcKey("mobs_sheared"), PersistentDataType.TAG_CONTAINER, mobsShearedPDC)
        pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
    }
}

class MobsShearedStat(item: ItemStack) : ItemStat(item) {
    override fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int) {
        val mobsShearedPDC = pdc.get(pdcKey("mobs_sheared"), PersistentDataType.TAG_CONTAINER)
        if (mobsShearedPDC != null) {
            val entities = mutableMapOf<NamespacedKey, Long>()
            var total = 0L

            mobsShearedPDC.keys.forEach { key ->
                val value = mobsShearedPDC.getOrDefault(key, PersistentDataType.LONG, 0)
                total += value
                entities[key] = value
            }

            lore.add("<gray>Mobs Sheared: ${total.toFormattedString(player.locale())}".toComponent())

            entities.entries
                .sortedByDescending { it.value }
                .take(maxElements)
                .forEach { (key, value) ->
                    lore.add(" <dark_gray>-</dark_gray> <gray><lang:entity.${key.namespace().lowercase()}.${key.value().lowercase()}>: ${value.toFormattedString(player.locale())}".toComponent())
                }

            if (entities.entries.size > maxElements) {
                lore.add(" <dark_gray>- (${entities.entries.size - maxElements} more)".toComponent())
            }
        }
    }
}