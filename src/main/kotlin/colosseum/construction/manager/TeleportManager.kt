package colosseum.construction.manager

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.utility.UtilWorld.locToStrClean
import colosseum.utility.MapConstants.WORLD
import colosseum.utility.MapConstants.WORLD_LOBBY
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.logging.*

@Suppress("MemberVisibilityCanBePrivate")
@ManagerDependency(WorldManager::class, MapDataManager::class)
class TeleportManager : ConstructionSiteManager("Teleport") {
    private lateinit var serverSpawnLocation: Location

    private fun getMapDataManager(): MapDataManager {
        return ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
    }

    override fun register() {
        var spawnWorld = WorldUtils.loadWorld(WORLD)
        if (spawnWorld == null) {
            spawnWorld = WorldUtils.loadWorld(WORLD_LOBBY) ?: throw Error("Where's spawn?")
        }
        spawnWorld.difficulty = Difficulty.PEACEFUL
        spawnWorld.setGameRuleValue("mobGriefing", "false")
        spawnWorld.setGameRuleValue("doMobSpawning", "false")
        spawnWorld.setGameRuleValue("doFireTick", "false")
        spawnWorld.setGameRuleValue("doDaylightCycle", "false")
        spawnWorld.isAutoSave = false
        spawnWorld.time = 6000
        spawnWorld.pvp = false
        serverSpawnLocation = Location(spawnWorld, 0.0, 106.0, 0.0)
        spawnWorld.setSpawnLocation(0, 106, 0)
    }

    override fun unregister() {
        try {
            WorldUtils.unloadWorld(serverSpawnLocation.world, false)
        } catch (e: Exception) {
            ConstructionSiteProvider.getSite().pluginLogger.log(Level.WARNING, "Error whilst unloading a world", e)
        }
    }

    fun teleportPlayer(player: Player, destination: Player): Boolean {
        return teleportPlayer(player, destination.location)
    }

    fun teleportToServerSpawn(player: Player): Boolean {
        if (teleportPlayer(player, serverSpawnLocation)) {
            player.gameMode = GameMode.ADVENTURE
            player.isFlying = false
            return true
        }
        return false
    }

    fun teleportPlayer(player: Player, destination: Location): Boolean {
        if (!check(player, destination)) {
            return false
        }
        if (player.isDead) {
            ConstructionSiteProvider.getSite().pluginLogger.warning("Cannot teleport ${player.name}. Why are they dead?")
            return false
        }
        return player.teleport(destination).also { v ->
            if (v) {
                ConstructionSiteProvider.getSite().pluginLogger.info(
                    "Teleported ${player.name} to ${
                        WorldUtils.getWorldRelativePath(
                            destination.world
                        )
                    } ${locToStrClean(destination)}"
                )
            } else {
                ConstructionSiteProvider.getSite().pluginLogger.warning("Internal failure whilst teleporting ${player.name}!")
            }
        }
    }

    fun check(player: Player, destination: Location): Boolean {
        if (player.world == destination.world) {
            return true
        }
        val path = WorldUtils.getWorldRelativePath(destination.world)
        return when (path.lowercase()) {
            WORLD_LOBBY, WORLD -> true
            else -> getMapDataManager().get(destination.world).allows(player)
        }
    }

    fun getSpawnLocation(): Location {
        return serverSpawnLocation.clone()
    }
}
