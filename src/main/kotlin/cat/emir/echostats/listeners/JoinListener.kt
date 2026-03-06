package cat.emir.echostats.listeners

import cat.emir.echostats.EchoStats
import cat.emir.echostats.item.LorePacketInterceptor
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener(val plugin: EchoStats) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val craftPlayer = event.player as CraftPlayer
        val pipeline = craftPlayer.handle.connection.connection.channel.pipeline()

        pipeline.addBefore("packet_handler", "${craftPlayer.name}_lore_handler", LorePacketInterceptor(craftPlayer))
    }
}