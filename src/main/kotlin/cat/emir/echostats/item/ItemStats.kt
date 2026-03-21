package cat.emir.echostats.item

import cat.emir.echostats.EchoStats
import cat.emir.echostats.stats.ArrowsShotStat
import cat.emir.echostats.stats.AttacksBlockedStat
import cat.emir.echostats.stats.BlocksBrokenStat
import cat.emir.echostats.stats.DistanceFlownStat
import cat.emir.echostats.stats.KillsStat
import cat.emir.echostats.stats.MobsShearedStat
import cat.emir.echostats.stats.TimesFishedStat
import io.papermc.paper.persistence.PersistentDataContainerView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.text.NumberFormat
import java.util.Locale

fun PersistentDataContainer.getTagContainer(key: NamespacedKey): PersistentDataContainer = this.getOrDefault(
    key, PersistentDataType.TAG_CONTAINER, this.adapterContext.newPersistentDataContainer()
)

fun PersistentDataContainerView.getTagContainer(key: NamespacedKey): PersistentDataContainer = this.getOrDefault(
    key, PersistentDataType.TAG_CONTAINER, this.adapterContext.newPersistentDataContainer()
)

fun Number.toFormattedString(locale: Locale): String {
    return NumberFormat.getInstance(locale).format(this)
}

fun String.toComponent() = MiniMessage.miniMessage().deserialize(this)

class ItemStats(val item: ItemStack) {
    companion object {
        fun pdcKey(string: String) = NamespacedKey(EchoStats.instance, string)
        fun hasStats(item: ItemStack) = item.persistentDataContainer.has(pdcKey("stats"))
    }

    fun buildLore(player: Player): List<Component> {
        val pdc = item.persistentDataContainer.getTagContainer(pdcKey("stats"))
        val showMore = pdc.getOrDefault(pdcKey("show_more"), PersistentDataType.BOOLEAN, false)
        val maxElements = if (showMore) 999 else 3
        val lore = mutableListOf<Component>()

        listOf(
            BlocksBrokenStat(item),
            KillsStat(item),
            MobsShearedStat(item),
            ArrowsShotStat(item),
            AttacksBlockedStat(item),
            TimesFishedStat(item),
            DistanceFlownStat(item)
        ).forEach { it.modifyLore(player, pdc, lore, showMore, maxElements) }

        if (lore.isNotEmpty()) {
            val topLore = mutableListOf<Component>()

            if (!showMore) {
                if (item.lore()?.isNotEmpty() ?: false)
                    topLore.addAll(item.lore()!!)

                if (item.enchantments.isNotEmpty() || item.lore()?.isNotEmpty() ?: false)
                    topLore.add("".toComponent())

                if (topLore.isNotEmpty())
                    lore.addAll(0, topLore)
            }

            lore.add("".toComponent())
            lore.add(("<dark_gray><key:key.sneak> + <key:key.use> to " + (if (showMore) "view less." else "view more.")).toComponent())
        } else {
            if (item.lore()?.isNotEmpty() ?: false)
                return item.lore()!!
        }

        return lore.map {
            it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        }
    }

    fun toggleMoreView() {
        item.editPersistentDataContainer { pdc ->
            val statsPDC = pdc.getTagContainer(pdcKey("stats"))
            val value = statsPDC.getOrDefault(pdcKey("show_more"), PersistentDataType.BOOLEAN, false)

            statsPDC.set(pdcKey("show_more"), PersistentDataType.BOOLEAN, !value)
            pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
        }
    }
}