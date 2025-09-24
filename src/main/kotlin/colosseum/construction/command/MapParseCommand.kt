package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.ParseManager
import colosseum.construction.manager.TeleportManager
import colosseum.utility.UtilPlayerBase
import colosseum.utility.arcade.GameType
import org.apache.commons.lang.StringUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MapParseCommand: AbstractMapAdminCommand(
    listOf("mapparse"),
    "Parse map. Radius is 200 by default.",
    "/mapparse <radius> [optional arguments]"
) {
    private fun getParseManager(): ParseManager {
        return ConstructionSiteProvider.getSite().getManager(ParseManager::class.java)
    }

    override fun canRun(console: CommandSender): Boolean {
        return true
    }

    override fun canRun(caller: Player): Boolean {
        val parseManager = getParseManager()
        var r = false
        parseManager.isRunning.also {
            r = it
            if (it) {
                UtilPlayerBase.sendMessage(caller, String.format("&cA parse task is running. Current progress %.2f%%", parseManager.progress * 100.0))
            }
        }
        return super.canRun(caller) && !r
    }

    override fun runConstruction(sender: CommandSender, label: String, args: Array<String>): Boolean {
        val parseManager = getParseManager()
        parseManager.isRunning.also {
            if (it) {
                UtilPlayerBase.sendMessage(sender, String.format("A parse task is running. Current progress %.2f%%", parseManager.progress * 100.0))
            } else {
                UtilPlayerBase.sendMessage(sender, "There's no parse task running.")
            }
        }
        return true
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        var args0 = args.clone()
        var radius = 200

        // Radius settings
        if (args0.isNotEmpty()) {
            try {
                radius = args0[0].toInt()
                if (radius < 1) {
                    UtilPlayerBase.sendMessage(caller, "&cRadius must be no less than 1")
                    return true
                }
            } catch (ex: NumberFormatException) {
                return false
            }

            args0 = if (args0.size > 1) {
                // If there are more args, set the new args
                // to everything past the radius
                args0.slice(1 until args0.size).toTypedArray()
            } else {
                // Otherwise, set it to an empty array.
                arrayOf()
            }
        }

        val parseLoc = caller.location
        val world = parseLoc.world

        val data = getMapDataManager().get(world)
        if (StringUtils.isEmpty(data.mapName) || StringUtils.isEmpty(data.mapCreator) || data.mapGameType == GameType.None) {
            UtilPlayerBase.sendMessage(caller, "&cMap Name/Author/GameType are not set!")
            return true
        }

        val worldManager = getWorldManager()
        val path = worldManager.getWorldRelativePath(world)
        // Teleport players out
        for (other in world.players) {
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java).teleportToServerSpawn(other)
        }
        Command.broadcastCommandMessage(caller, "Parse ${data.mapName} (${data.mapGameType.name})", true)
        ConstructionSiteProvider.getSite().pluginLogger.info("${caller.name} requested parsing $path")
        getParseManager().schedule(world, args0.asList(), parseLoc, radius)
        return true
    }
}
