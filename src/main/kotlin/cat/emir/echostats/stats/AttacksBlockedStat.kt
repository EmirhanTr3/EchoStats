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

fun ItemStats.increaseAttacksBlocked(entity: LivingEntity) {
    item.editPersistentDataContainer { pdc ->
        val statsPDC = pdc.getTagContainer(pdcKey("stats"))
        val attacksBlockedPDC = statsPDC.getTagContainer(pdcKey("attacks_blocked"))
        val value = attacksBlockedPDC.getOrDefault(entity.type.key, PersistentDataType.LONG, 0)

        attacksBlockedPDC.set(entity.type.key, PersistentDataType.LONG, value + 1)
        statsPDC.set(pdcKey("attacks_blocked"), PersistentDataType.TAG_CONTAINER, attacksBlockedPDC)
        pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
    }
}

class AttacksBlockedStat(item: ItemStack) : ItemStat(item) {
    override fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int) {
        val attacksBlockedPDC = pdc.get(pdcKey("attacks_blocked"), PersistentDataType.TAG_CONTAINER)
        if (attacksBlockedPDC != null) {
            val entities = mutableMapOf<NamespacedKey, Long>()
            var total = 0L

            attacksBlockedPDC.keys.forEach { key ->
                val value = attacksBlockedPDC.getOrDefault(key, PersistentDataType.LONG, 0)
                total += value
                entities[key] = value
            }

            lore.add("<gray>Attacks Blocked: ${total.toFormattedString(player.locale())}".toComponent())

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