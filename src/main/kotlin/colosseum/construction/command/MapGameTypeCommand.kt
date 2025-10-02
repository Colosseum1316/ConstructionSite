package colosseum.construction.command

import colosseum.construction.BaseUtils
import colosseum.construction.BaseUtils.getGameTypes
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.PluginUtils
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import colosseum.utility.arcade.GameType
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class MapGameTypeCommand: AbstractMapAdminCommand(
    listOf("mapgametype"),
    "Set GameType for map.",
    "/mapgametype <gametype>"
), TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size == 1) {
            return StringUtil.copyPartialMatches(args[0], getGameTypes().map { v -> v.name }, ArrayList())
        }
        return null
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.size != 1) {
            return false
        }

        val world = caller.world
        val worldManager = getWorldManager()
        val worldFolder = worldManager.getWorldFolder(world)
        val path = worldManager.getWorldRelativePath(worldFolder)
        val data = getMapDataManager().get(world) as MutableMapData
        val newGameType = BaseUtils.determineGameType(args[0], true)
        if (newGameType == GameType.None) {
            PluginUtils.printValidGameTypes(caller)
            return true
        }
        ConstructionSiteProvider.getScheduler().scheduleAsync {
            data.updateAndWrite(FinalizedMapData(null, null, newGameType, data.isLive))
            Command.broadcastCommandMessage(caller, "Map ${data.mapName}: Set GameType to ${data.mapGameType.name}", true)
            ConstructionSiteProvider.getSite().pluginLogger.info("World $path: Set GameType to ${data.mapGameType.name}")
        }
        return true
    }
}
