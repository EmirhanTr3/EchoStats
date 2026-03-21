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

/**
 * will increase kills;players;player_uuid if entity is instance of player
 */
fun ItemStats.increaseKill(entity: LivingEntity) {
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

class KillsStat(item: ItemStack) : ItemStat(item) {
    override fun modifyLore(player: Player, pdc: PersistentDataContainer, lore: MutableList<Component>, showMore: Boolean, maxElements: Int) {
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

            lore.add("<gray>Kills: ${total.toFormattedString(player.locale())}".toComponent())

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
                    lore.add(" <dark_gray>-</dark_gray> <gray>$key: ${value.toFormattedString(player.locale())}".toComponent())
                }

            if (kills.entries.size > maxElements) {
                lore.add(" <dark_gray>- (${kills.entries.size - maxElements} more)".toComponent())
            }
        }
    }
}