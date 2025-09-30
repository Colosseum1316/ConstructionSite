package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.entity.Player
import java.util.function.*

class MapNameCommand: AbstractMapCreditCommand(
    listOf("mapname"),
    "Set map name.",
    "/mapname <Map name>"
) {
    override fun determineContent(caller: Player, alias: String, args: Array<String>): String {
        return content(args).toString()
    }

    override fun setField(
        caller: Player,
        alias: String,
        args: Array<String>,
        contentSupplier: Supplier<String>,
        mapDataSupplier: Supplier<MutableMapData>
    ): Boolean {
        val world = caller.world
        val worldManager = getWorldManager()
        val path = worldManager.getWorldRelativePath(world)
        val data = mapDataSupplier.get()
        val newMapName = contentSupplier.get()
        Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin()) {
            data.updateAndWrite(FinalizedMapData(newMapName, null, null, data.isLive))
            Command.broadcastCommandMessage(caller, "World $path set map name: ${mapDataSupplier.get().mapName}", true)
        }
        return true
    }
}
