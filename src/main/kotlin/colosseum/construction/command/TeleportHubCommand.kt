package colosseum.construction.command

import org.bukkit.entity.Player

class TeleportHubCommand : AbstractTeleportCommand(
    listOf("hub", "lobby"),
    "Go back to hub.",
    "/hub"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (!getTeleportManager().teleportToServerSpawn(caller)) {
            sayTeleportFail(caller)
        }
        return true
    }
}
