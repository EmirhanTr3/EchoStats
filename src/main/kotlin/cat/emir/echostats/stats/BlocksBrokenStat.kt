package cat.emir.echostats.stats

import cat.emir.echostats.item.ItemStat
import cat.emir.echostats.item.ItemStats
import cat.emir.echostats.item.ItemStats.Companion.pdcKey
import cat.emir.echostats.item.getTagContainer
import cat.emir.echostats.item.toComponent
import cat.emir.echostats.item.toFormattedString
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.component1
import kotlin.collections.component2

fun ItemStats.increaseBlocksBroken(type: Material) {
    item.editPersistentDataContainer { pdc ->
        val statsPDC = pdc.getTagContainer(pdcKey("stats"))
        val blocksBrokenPDC = statsPDC.getTagContainer(pdcKey("blocks_broken"))
        val value = blocksBrokenPDC.getOrDefault(type.key, PersistentDataType.LONG, 0)

        blocksBrokenPDC.set(type.key, PersistentDataType.LONG, value + 1)
        statsPDC.set(pdcKey("blocks_broken"), PersistentDataType.TAG_CONTAINER, blocksBrokenPDC)
        pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
    }
}

class BlocksBrokenStat(item: ItemStack) : ItemStat(item) {
    override fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int) {
        val blocksBrokenPDC = pdc.get(pdcKey("blocks_broken"), PersistentDataType.TAG_CONTAINER)
        if (blocksBrokenPDC != null) {
            val blocks = mutableMapOf<NamespacedKey, Long>()
            var total = 0L

            blocksBrokenPDC.keys.forEach { key ->
                val value = blocksBrokenPDC.getOrDefault(key, PersistentDataType.LONG, 0)
                total += value
                blocks[key] = value
            }

            lore.add("<gray><lang:stat_type.minecraft.mined>: ${total.toFormattedString(player.locale())}".toComponent())

            blocks.entries
                .sortedByDescending { it.value }
                .take(maxElements)
                .forEach { (key, value) ->
                    lore.add(
                        " <dark_gray>-</dark_gray> <gray><lang:block.${key.namespace().lowercase()}.${
                            key.value().lowercase()
                        }>: ${value.toFormattedString(player.locale())}".toComponent()
                    )
                }

            if (blocks.entries.size > maxElements) {
                lore.add(" <dark_gray>- (${blocks.entries.size - maxElements} more)".toComponent())
            }
        }
    }
}