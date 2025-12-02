package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import com.google.common.collect.ImmutableSet
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
        Create a new map.
        -v: Void map in Overworld.
        -n: Void map in Nether.
        -e: Void map in The End.
    """.trimIndent(),
    "/newmap [optional arg]"
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

    override fun onTabComplete(
        sender: CommandSender?,
        command: Command?,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size == 1) {
            return StringUtil.copyPartialMatches(args[0], listOf(VOID_OVERWORLD, VOID_NETHER, VOID_END), ArrayList())
        }
        return null
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.size > 1) {
            return false
        }

        val worldFolderName = "${caller.name}-${System.nanoTime()}"
        var worldCreator = WorldUtils.getWorldCreator(WorldUtils.getWorldRelativePath(WorldUtils.getSingleWorldRootPath(worldFolderName)))
        worldCreator.type(WorldType.FLAT)
        worldCreator.generateStructures(false)

        var voidEnd = false
        var voidNether = false
        var voidOverworld = false
        var generateVoidWorld = false
        if (args.size == 1) {
            voidEnd = args[0].equals(VOID_END, ignoreCase = true)
            voidNether = args[0].equals(VOID_NETHER, ignoreCase = true)
            voidOverworld = args[0].equals(VOID_OVERWORLD, ignoreCase = true)
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

        ConstructionSiteProvider.getScheduler().schedule {
            val world = WorldUtils.createOrLoadWorld(worldCreator) ?: throw RuntimeException("Could not create world ${worldCreator.name()}")
            world.difficulty = Difficulty.EASY
            world.setSpawnLocation(0, 106, 0)
            world.setGameRuleValue("mobGriefing", "false")
            world.setGameRuleValue("doMobSpawning", "false")
            world.setGameRuleValue("doFireTick", "false")
            world.setGameRuleValue("doDaylightCycle", "false")
            world.time = 6000
            world.pvp = false

            ConstructionSiteProvider.getScheduler().scheduleAsync {
                val mapData = getMapDataManager().get(world) as MutableMapData
                mapData.update(FinalizedMapData(worldFolderName, caller.name, ImmutableSet.of(caller.uniqueId)))
                ConstructionSiteProvider.getScheduler().schedule {
                    getTeleportManager().teleportPlayer(caller, world.spawnLocation)
                    caller.gameMode = GameMode.CREATIVE
                    caller.isFlying = true
                    Command.broadcastCommandMessage(caller, "Create new ${if (generateVoidWorld) "void " else ""}world: ${worldCreator.name()}", true)
                }
            }
        }
        return true
    }
}