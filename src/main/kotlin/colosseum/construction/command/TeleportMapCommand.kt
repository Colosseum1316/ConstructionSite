package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.utility.UtilPlayerBase
import colosseum.utility.UtilWorld
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.util.*

class TeleportMapCommand: AbstractTeleportCommand(
    listOf("tpmap"),
    "Teleport to a map by world uuid.",
    "/tpmap <world uuid>"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.size > 1) {
            return false
        }

        val available = HashMap<UUID, String>()
        val mapDataManager = getMapDataManager()
        WorldUtils.getMapsRootPath().listFiles()?.forEach { f ->
            if (f.isDirectory) {
                UtilWorld.getWorld(WorldUtils.getWorldRelativePath(f))?.let { world ->
                    val data = mapDataManager.get(world)
                    available[world.uid] = "${data.mapName} - ${data.mapCreator} (${data.mapGameType.name})"
                }
            }
        }

        if (args.isEmpty()) {
            for (entry in available) {
                val message = TextComponent(entry.value + ": ")
                message.color = ChatColor.GRAY
                val message0 = TextComponent(entry.key.toString())
                message0.color = ChatColor.YELLOW
                message0.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tpmap ${entry.key}")
                message0.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    arrayOf(TextComponent("Click to run the command!"))
                )
                message.addExtra(message0)
                caller.spigot().sendMessage(message)
            }
            if (available.isEmpty()) {
                UtilPlayerBase.sendMessage(caller, "&cNo maps available!")
            }
            return true
        } else {
            try {
                val key = UUID.fromString(args[0])
                if (!available.contains(key)) {
                    UtilPlayerBase.sendMessage(caller, "&cUnknown world!")
                    return false
                }
                val world = WorldUtils.getWorldByUUID(key) ?: run {
                    UtilPlayerBase.sendMessage(caller, "&cThere's no world with that UUID!")
                    return false
                }
                if (getTeleportManager().teleportPlayer(caller, world.spawnLocation)) {
                    caller.gameMode = GameMode.CREATIVE
                    caller.isFlying = true
                    ConstructionSiteProvider.getSite().pluginLogger.info("Teleported ${caller.name} to ${WorldUtils.getWorldRelativePath(world)}")
                } else {
                    sayTeleportFail(caller)
                }
                return true
            } catch (e: IllegalArgumentException) {
                UtilPlayerBase.sendMessage(caller, "&cInvalid UUID!")
                return false
            }
        }
    }
}