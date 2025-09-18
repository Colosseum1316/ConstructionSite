package colosseum.construction.command

import colosseum.construction.BaseUtils
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.WorldManager
import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

abstract class AbstractMapAdminCommand protected constructor(
    aliases: List<String>,
    description: String,
    usage: String
): ConstructionSiteCommand(
    aliases, description, usage
) {
    protected fun getWorldManager(): WorldManager {
        return ConstructionSiteProvider.getSite().getManager(WorldManager::class.java)
    }

    protected fun getMapDataManager(): MapDataManager {
        return ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
    }

    override fun canRun(caller: Player): Boolean {
        val world = caller.world
        val worldManager = getWorldManager()
        val mapDataManager = getMapDataManager()
        val path = worldManager.getWorldRelativePath(world)
        if (BaseUtils.isLevelNamePreserved(path)) {
            UtilPlayerBase.sendMessage(caller, "&cYou are in \"$path\"!")
            return false
        }
        return mapDataManager.get(world).allows(caller)
    }
}
