package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import colosseum.utility.UtilPlayerBase
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.apache.commons.io.FileUtils
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.entity.Player
import java.util.*

class MapDeleteCommand: AbstractMapAdminCommand(
    listOf("mapdelete"),
    "Delete map by world uuid. " + ChatColor.RED + "No further confirmation.",
    "/mapdelete <world uuid>"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        val world = caller.world
        if (args.isEmpty()) {
            val message = TextComponent(world.uid.toString())
            message.color = net.md_5.bungee.api.ChatColor.YELLOW
            message.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("Click to run the command!")))
            message.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mapdelete ${world.uid}")
            caller.spigot().sendMessage(message)
            return true
        }
        if (args.size != 1) {
            return false
        }
        val uuid: UUID
        try {
            uuid = UUID.fromString(args[0])
            if (uuid != world.uid) {
                UtilPlayerBase.sendMessage(caller, "&cUUID mismatch!")
                return true
            }
            val worldManager = getWorldManager()
            val f = worldManager.getWorldFolder(world)
            val site = ConstructionSiteProvider.getSite()
            for (other in world.players) {
                site.getManager(TeleportManager::class.java).teleportToServerSpawn(other)
            }
            worldManager.unloadWorld(world, false)
            site.getManager(MapDataManager::class.java).discard(world)
            Command.broadcastCommandMessage(caller, "Deleting world ${worldManager.getWorldRelativePath(f)}", true)
            ConstructionSiteProvider.getScheduler().scheduleAsync {
                FileUtils.deleteQuietly(f)
            }
            return true
        } catch (e: IllegalArgumentException) {
            UtilPlayerBase.sendMessage(caller, "&cInvalid input!")
            return false
        }
    }
}