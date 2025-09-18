package colosseum.construction.command

import colosseum.construction.BaseUtils
import colosseum.construction.BaseUtils.getGameTypes
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.PluginUtils
import colosseum.construction.data.MutableMapData
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import colosseum.construction.manager.WorldManager
import colosseum.utility.arcade.GameType
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.generator.ChunkGenerator
import org.bukkit.util.StringUtil
import java.util.*

class NewMapCommand: AbstractOpCommand(
    listOf("newmap"),
    """
        Create a new map with a set Gametype. 
        -v: Void map in Overworld.
        -n: Void map in Nether.
        -e: Void map in The End.
    """.trimIndent(),
    "/newmap <gametype> [optional args]"
), TabCompleter {
    companion object {
        private val VOID_OVERWORLD = "-v"
        private val VOID_NETHER = "-n"
        private val VOID_END = "-e"

        class VoidGenerator: ChunkGenerator() {
            override fun generateChunkData(
                world: World,
                random: Random,
                x: Int,
                z: Int,
                biome: BiomeGrid
            ): ChunkData {
                return createChunkData(world)
            }
        }
    }

    private fun getTeleportManager(): TeleportManager {
        return ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
    }

    private fun getMapDataManager(): MapDataManager {
        return ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
    }

    private fun getWorldManager(): WorldManager {
        return ConstructionSiteProvider.getSite().getManager(WorldManager::class.java)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size == 1) {
            return StringUtil.copyPartialMatches(args[0], getGameTypes().map { v -> v.name }, ArrayList())
        }
        if (args.size == 2) {
            return StringUtil.copyPartialMatches(args[1], listOf(VOID_OVERWORLD, VOID_NETHER, VOID_END), ArrayList())
        }
        return null
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.isEmpty() || args.size > 2) {
            return false
        }
        val gameType = BaseUtils.determineGameType(args[0], true)
        if (gameType == GameType.None) {
            PluginUtils.printValidGameTypes(caller)
            return true
        }

        val worldFolderName = "${gameType.name}-${caller.name}-${System.nanoTime()}"
        val worldManager = getWorldManager()
        var worldCreator = worldManager.getWorldCreator(worldManager.getWorldRelativePath(worldManager.getSingleWorldRootPath(worldFolderName)))
        worldCreator.type(WorldType.FLAT)
        worldCreator.generateStructures(false)

        var voidEnd = false
        var voidNether = false
        var voidOverworld = false
        var generateVoidWorld = false
        if (args.size == 2) {
            voidEnd = args[1].equals(VOID_END, ignoreCase = true)
            voidNether = args[1].equals(VOID_NETHER, ignoreCase = true)
            voidOverworld = args[1].equals(VOID_OVERWORLD, ignoreCase = true)
        }
        generateVoidWorld = voidNether || voidEnd || voidOverworld

        if (generateVoidWorld) {
            worldCreator.generatorSettings("3;minecraft:air;2")
            if (voidOverworld) {
                worldCreator.environment(World.Environment.NORMAL)
            } else if (voidEnd) {
                worldCreator.environment(World.Environment.THE_END)
                worldCreator = worldCreator.generator(VoidGenerator())
            } else if (voidNether) {
                worldCreator.environment(World.Environment.NETHER)
                worldCreator = worldCreator.generator(VoidGenerator())
            }
        }

        val world = worldManager.createOrLoadWorld(worldCreator) ?: throw RuntimeException("Could not create world ${worldCreator.name()}")
        world.difficulty = Difficulty.EASY
        world.setSpawnLocation(0, 106, 0)
        world.setGameRuleValue("mobGriefing", "false")
        world.setGameRuleValue("doMobSpawning", "false")
        world.setGameRuleValue("doFireTick", "false")
        world.setGameRuleValue("doDaylightCycle", "false")
        world.time = 6000
        world.pvp = false

        val mapData = getMapDataManager().get(world) as MutableMapData
        mapData.mapName = worldFolderName
        mapData.mapCreator = caller.name
        mapData.adminList.add(caller.uniqueId)
        mapData.mapGameType = gameType
        getTeleportManager().teleportPlayer(caller, world.spawnLocation)
        caller.gameMode = GameMode.CREATIVE
        caller.isFlying = true
        Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin()) {
            mapData.write()
        }

        Command.broadcastCommandMessage(caller, "Create new ${if (generateVoidWorld) "void " else ""}world: ${worldCreator.name()}", true)
        return true
    }
}