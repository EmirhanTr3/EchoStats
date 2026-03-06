package cat.emir.echostats

import cat.emir.echostats.listeners.ItemStatsListeners
import cat.emir.echostats.listeners.JoinListener
import org.bukkit.plugin.java.JavaPlugin

class EchoStats : JavaPlugin() {
    companion object {
        lateinit var instance: EchoStats
    }

    override fun onEnable() {
        instance = this

        server.pluginManager.registerEvents(JoinListener(this), this)
        server.pluginManager.registerEvents(ItemStatsListeners(this), this)
    }

}
