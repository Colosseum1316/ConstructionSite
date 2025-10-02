package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import colosseum.utility.UtilPlayerBase.searchOnline
import com.google.common.collect.ImmutableSet
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
            val adminList = data.adminList().toMutableSet()
            var add = false
            if (adminList.add(target.uniqueId)) {
                add = true
            } else {
                adminList.remove(target.uniqueId)
            }
            ConstructionSiteProvider.getScheduler().scheduleAsync({
                data.updateAndWrite(FinalizedMapData(null, null, null, ImmutableSet.copyOf(adminList), data.isLive))
                if (add) {
                    Command.broadcastCommandMessage(caller, "${target.name} is now admin in ${data.mapName}", true)
                    ConstructionSiteProvider.getSite().pluginLogger.info("World $path: ${target.name} is now admin in ${data.mapName}")
                } else {
                    Command.broadcastCommandMessage(caller, "${target.name} is no longer admin in ${data.mapName}", true)
                    ConstructionSiteProvider.getSite().pluginLogger.info("World $path: ${target.name} is no longer admin in ${data.mapName}")
                }
            }, Void::class.java)
        }
        return true
    }
}
