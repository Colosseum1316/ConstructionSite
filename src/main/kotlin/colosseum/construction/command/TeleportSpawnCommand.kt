package colosseum.construction.command

import colosseum.utility.UtilPlayerBase
import colosseum.utility.UtilWorld.locToStrClean
import org.bukkit.entity.Player

class TeleportSpawnCommand: AbstractTeleportCommand(
    listOf("spawn"),
    "Teleport to world spawn point.",
    "/spawn"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        val world = caller.world
        val location = world.spawnLocation
        if (getTeleportManager().teleportPlayer(caller, location)) {
            UtilPlayerBase.sendMessage(caller, "Teleported to ${locToStrClean(location)}")
        } else {
            sayTeleportFail(caller)
        }
        return true
    }
}
