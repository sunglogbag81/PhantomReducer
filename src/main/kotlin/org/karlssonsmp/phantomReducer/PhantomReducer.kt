package org.karlssonsmp.phantomReducer

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.entity.EntityType
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.concurrent.ThreadLocalRandom
import org.bstats.bukkit.Metrics

class PhantomReducer : JavaPlugin(), Listener {

    private companion object {
        private val ALLOWED_SPAWN_REASONS = setOf(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.PATROL
        )

        private const val DEFAULT_SPAWN_RATE = 0.25
        private const val BSTATS_PLUGIN_ID = 26992
    }

    @Volatile
    private var spawnRate: Double = DEFAULT_SPAWN_RATE

    private val prefix = "§9§lᴘʜᴀɴᴛᴏᴍʀᴇᴅᴜᴄᴇʀ §7» §r"

    override fun onEnable() {
        saveDefaultConfig()

        spawnRate = loadSpawnRate()
        val metricsEnabled = config.getBoolean("metrics", true)

        server.pluginManager.registerEvents(this, this)

        if (metricsEnabled) {
            Metrics(this, BSTATS_PLUGIN_ID)
        }

        logger.info("PhantomReducer enabled! Phantom spawn rate set to ${(spawnRate * 100).toInt()}%.")
    }

    override fun onDisable() {
        logger.info("PhantomReducer disabled!")
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPhantomSpawn(event: CreatureSpawnEvent) {
        if (event.entityType != EntityType.PHANTOM) return

        if (event.spawnReason !in ALLOWED_SPAWN_REASONS) return

        if (ThreadLocalRandom.current().nextDouble() >= spawnRate) {
            event.isCancelled = true
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (command.name.equals("phantomreducer", ignoreCase = true)) {
            if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
                if (!sender.hasPermission("phantomreducer.reload")) {
                    sender.sendMessage("${prefix}§cYou don't have permission to do that.")
                    return true
                }

                reloadConfig()
                spawnRate = loadSpawnRate()
                sender.sendMessage("${prefix}§aConfig reloaded! Spawn rate: ${(spawnRate * 100).toInt()}%")
                return true
            } else {
                sender.sendMessage("${prefix}§7Usage: /phantomreducer reload")
                return true
            }
        }
        return false
    }

    private fun loadSpawnRate(): Double =
        config.getDouble("phantom-spawn-rate", DEFAULT_SPAWN_RATE).coerceIn(0.0, 1.0)
}
