package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

abstract class AbstractTeleportCommand protected constructor(
    aliases: List<String>,
    description: String,
    usage: String
) : ConstructionSiteCommand(
    aliases, description, usage
) {
    protected fun getTeleportManager(): TeleportManager {
        return ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
    }

    protected fun getMapDataManager(): MapDataManager {
        return ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
    }

    protected fun sayTeleportFail(player: Player) {
        UtilPlayerBase.sendMessage(player, "&cTeleportation unsuccessful...")
    }
}
