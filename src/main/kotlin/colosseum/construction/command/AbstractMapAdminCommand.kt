package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.manager.MapDataManager
import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

abstract class AbstractMapAdminCommand protected constructor(
    aliases: List<String>,
    description: String,
    usage: String
) : ConstructionSiteCommand(
    aliases, description, usage
) {
    protected fun getMapDataManager(): MapDataManager {
        return ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
    }

    override fun canRun(caller: Player): Boolean {
        val world = caller.world
        val mapDataManager = getMapDataManager()
        val path = WorldUtils.getWorldRelativePath(world)
        if (WorldUtils.isLevelNamePreserved(path)) {
            UtilPlayerBase.sendMessage(caller, "&cYou are in \"$path\"!")
            return false
        }
        return mapDataManager.get(world).allows(caller)
    }
}
