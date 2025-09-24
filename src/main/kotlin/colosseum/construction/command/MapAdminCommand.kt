package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.data.MutableMapData
import colosseum.utility.UtilPlayerBase.searchOnline
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.entity.Player

class MapAdminCommand: AbstractMapAdminCommand(
    listOf("mapadmin"),
    "Set a player as an admin for map, or remove.",
    "/mapadmin <player>"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.size != 1) {
            return false
        }
        val target = searchOnline(caller, args[0], true)
        if (target != null) {
            val world = caller.world
            val worldManager = getWorldManager()
            val data = getMapDataManager().get(world) as MutableMapData
            val path = worldManager.getWorldRelativePath(world)
            if (data.adminList.add(target.uniqueId)) {
                Command.broadcastCommandMessage(caller, "${target.name} is now admin in ${data.mapName}", true)
                ConstructionSiteProvider.getSite().pluginLogger.info("World $path: ${target.name} is now admin in ${data.mapName}")
            } else {
                data.adminList.remove(target.uniqueId)
                Command.broadcastCommandMessage(caller, "${target.name} is no longer admin in ${data.mapName}", true)
                ConstructionSiteProvider.getSite().pluginLogger.info("World $path: ${target.name} is no longer admin in ${data.mapName}")
            }
            Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin()) {
                data.write()
            }
        }
        return true
    }
}
