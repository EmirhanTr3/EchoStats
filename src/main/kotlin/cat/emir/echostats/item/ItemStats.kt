package cat.emir.echostats.item

import cat.emir.echostats.EchoStats
import io.papermc.paper.persistence.PersistentDataContainerView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.text.NumberFormat
import java.util.Locale

class ItemStats(val item: ItemStack) {
    companion object {
        fun pdcKey(string: String) = NamespacedKey(EchoStats.instance, string)
        fun hasStats(item: ItemStack) = item.persistentDataContainer.has(pdcKey("stats"))
    }
    private fun mm(string: String) = MiniMessage.miniMessage().deserialize(string)

    private fun PersistentDataContainer.getTagContainer(key: NamespacedKey) = this.getOrDefault(
        key, PersistentDataType.TAG_CONTAINER, this.adapterContext.newPersistentDataContainer()
    )

    private fun PersistentDataContainerView.getTagContainer(key: NamespacedKey) = this.getOrDefault(
        key, PersistentDataType.TAG_CONTAINER, this.adapterContext.newPersistentDataContainer()
    )

    private fun Number.toFormattedString(locale: Locale): String {
        return NumberFormat.getInstance(locale).format(this)
    }

    fun buildLore(player: Player): List<Component> {
        val pdc = item.persistentDataContainer.getTagContainer(pdcKey("stats"))
        val showMore = pdc.getOrDefault(pdcKey("show_more"), PersistentDataType.BOOLEAN, false)
        val maxElements = if (showMore) 999 else 3
        val lore = mutableListOf<Component>()

        val blocksBrokenPDC = pdc.get(pdcKey("blocks_broken"), PersistentDataType.TAG_CONTAINER)
        if (blocksBrokenPDC != null) {
            val blocks = mutableMapOf<NamespacedKey, Long>()
            var total = 0L

            blocksBrokenPDC.keys.forEach { key ->
                val value = blocksBrokenPDC.getOrDefault(key, PersistentDataType.LONG, 0)
                total += value
                blocks[key] = value
            }

            lore.add(mm("<gray><lang:stat_type.minecraft.mined>: ${total.toFormattedString(player.locale())}"))

            blocks.entries
                .sortedByDescending { it.value }
                .take(maxElements)
                .forEach { (key, value) ->
                    lore.add(mm(" <dark_gray>-</dark_gray> <gray><lang:block.${key.namespace().lowercase()}.${key.value().lowercase()}>: ${value.toFormattedString(player.locale())}"))
                }

            if (blocks.entries.size > maxElements) {
                lore.add(mm(" <dark_gray>- (${blocks.entries.size - maxElements} more)"))
            }
        }

        val killsPDC = pdc.get(pdcKey("kills"), PersistentDataType.TAG_CONTAINER)
        if (killsPDC != null) {
            var total = 0L

            val playerKills = mutableMapOf<String, Long>()
            var playerTotal = 0L

            val mobKills = mutableMapOf<NamespacedKey, Long>()
            var mobTotal = 0L

            val playerKillsPDC = killsPDC.get(pdcKey("players"), PersistentDataType.TAG_CONTAINER)
            playerKillsPDC?.keys?.forEach { key ->
                val value = playerKillsPDC.getOrDefault(key, PersistentDataType.LONG, 0)
                playerKills[key.value()] = value
                playerTotal += value
            }

            val mobKillsPDC = killsPDC.get(pdcKey("mobs"), PersistentDataType.TAG_CONTAINER)
            mobKillsPDC?.keys?.forEach { key ->
                val value = mobKillsPDC.getOrDefault(key, PersistentDataType.LONG, 0)
                mobKills[key] = value
                mobTotal += value
            }

            total += playerTotal + mobTotal

            val kills = mutableMapOf<String, Long>()

            lore.add(mm("<gray>Kills: ${total.toFormattedString(player.locale())}"))

            if (playerKills.isNotEmpty()) {
                kills[if (playerKills.size > 1) "Players (${playerKills.size})" else "Player"] = playerTotal
//                playerKills.forEach { (key, value) ->
//                    val name = EchoStats.instance.server.getOfflinePlayer(UUID.fromString(key)).name ?: key
//                    kills[name] = value
//                }

//                lore.add(mm("<dark_gray>-</dark_gray> <gray>Player kills: $playerTotal"))
//                playerKills.entries
//                    .sortedByDescending { it.value }
//                    .take(3)
//                    .forEach { (key, value) ->
//                        val name = EchoStats.instance.server.getOfflinePlayer(UUID.fromString(key)).name ?: key
//                        lore.add(mm(" <dark_gray>-</dark_gray> <gray>$name: $value"))
//                    }
            }

            if (mobKills.isNotEmpty()) {
                mobKills.forEach { (key, value) ->
                    kills["<lang:entity.${key.namespace.lowercase()}.${key.value().lowercase()}>"] = value
                }
//                lore.add(mm("<dark_gray>-</dark_gray> <gray>Mob kills: $mobTotal"))
//                mobKills.entries
//                    .sortedByDescending { it.value }
//                    .take(3)
//                    .forEach { (key, value) ->
//                        lore.add(mm(" <dark_gray>-</dark_gray> <gray><lang:entity.${key.namespace.lowercase()}.${key.value().lowercase()}>: $value"))
//                    }
            }

            kills.entries
                .sortedByDescending { it.value }
                .take(maxElements)
                .forEach { (key, value) ->
                    lore.add(mm(" <dark_gray>-</dark_gray> <gray>$key: ${value.toFormattedString(player.locale())}"))
                }

            if (kills.entries.size > maxElements) {
                lore.add(mm(" <dark_gray>- (${kills.entries.size - maxElements} more)"))
            }
        }

        if (lore.isNotEmpty()) {
            val topLore = mutableListOf<Component>()

            if (!showMore) {
                if (item.lore()?.isNotEmpty() ?: false)
                    topLore.addAll(item.lore()!!)

                if (item.enchantments.isNotEmpty() || item.lore()?.isNotEmpty() ?: false)
                    topLore.add(mm(""))

                if (topLore.isNotEmpty())
                    lore.addAll(0, topLore)
            }

            lore.add(mm(""))
            lore.add(mm("<dark_gray><key:key.sneak> + <key:key.use> to " + (if (showMore) "view less." else "view more.")))
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

    fun increaseBlocksBroken(type: Material) {
        item.editPersistentDataContainer { pdc ->
            val statsPDC = pdc.getTagContainer(pdcKey("stats"))
            val blocksBrokenPDC = statsPDC.getTagContainer(pdcKey("blocks_broken"))
            val value = blocksBrokenPDC.getOrDefault(type.key, PersistentDataType.LONG, 0)

            blocksBrokenPDC.set(type.key, PersistentDataType.LONG, value + 1)
            statsPDC.set(pdcKey("blocks_broken"), PersistentDataType.TAG_CONTAINER, blocksBrokenPDC)
            pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
        }
    }

    /**
     * will increase kills;players;player_uuid if entity is instance of player
     */
    fun increaseKill(entity: LivingEntity) {
        item.editPersistentDataContainer { pdc ->
            val statsPDC = pdc.getTagContainer(pdcKey("stats"))
            val killsPDC = statsPDC.getTagContainer(pdcKey("kills"))

            if (entity is Player) {
                val playerKillsPDC = killsPDC.getTagContainer(pdcKey("players"))
                val playerKey = NamespacedKey("player", entity.uniqueId.toString())
                val value = playerKillsPDC.getOrDefault(playerKey, PersistentDataType.LONG, 0)

                playerKillsPDC.set(playerKey, PersistentDataType.LONG, value + 1)
                killsPDC.set(pdcKey("players"), PersistentDataType.TAG_CONTAINER, playerKillsPDC)
            } else  {
                val mobKillsPDC = killsPDC.getTagContainer(pdcKey("mobs"))
                val value = mobKillsPDC.getOrDefault(entity.type.key, PersistentDataType.LONG, 0)

                mobKillsPDC.set(entity.type.key, PersistentDataType.LONG, value + 1)
                killsPDC.set(pdcKey("mobs"), PersistentDataType.TAG_CONTAINER, mobKillsPDC)
            }

            statsPDC.set(pdcKey("kills"), PersistentDataType.TAG_CONTAINER, killsPDC)
            pdc.set(pdcKey("stats"), PersistentDataType.TAG_CONTAINER, statsPDC)
        }
    }
}