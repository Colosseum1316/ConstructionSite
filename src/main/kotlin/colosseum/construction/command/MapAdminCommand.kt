package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import colosseum.utility.UtilPlayerBase.searchOnline
import com.google.common.collect.ImmutableSet
import org.bukkit.command.Command
import org.bukkit.entity.Player

class MapAdminCommand : AbstractMapAdminCommand(
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
            val data = getMapDataManager().get(world) as MutableMapData
            val path = WorldUtils.getWorldRelativePath(world)
            val adminList = data.adminList().toMutableSet()
            var add = false
            if (adminList.add(target.uniqueId)) {
                add = true
            } else {
                adminList.remove(target.uniqueId)
            }
            ConstructionSiteProvider.getScheduler().scheduleAsync {
                data.update(FinalizedMapData(ImmutableSet.copyOf(adminList)))
                if (add) {
                    Command.broadcastCommandMessage(caller, "${target.name} is now admin in ${data.mapName}", true)
                    ConstructionSiteProvider.getSite().pluginLogger.info("World $path: ${target.name} is now admin in ${data.mapName}")
                } else {
                    Command.broadcastCommandMessage(
                        caller,
                        "${target.name} is no longer admin in ${data.mapName}",
                        true
                    )
                    ConstructionSiteProvider.getSite().pluginLogger.info("World $path: ${target.name} is no longer admin in ${data.mapName}")
                }
            }
        }
        return true
    }
}
