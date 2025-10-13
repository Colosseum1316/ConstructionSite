package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import colosseum.utility.UtilPlayerBase
import colosseum.utility.UtilWorld.locToStrClean
import colosseum.utility.UtilWorld.vecToStrClean
import com.google.common.collect.ImmutableMap
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class TeleportWarpCommand: AbstractTeleportCommand(
    listOf("warp"),
    "Get/Set warp points in map.",
    """
        /warp <warp name>  Teleport to a warp point.
        /warp list  Get all known warp points.
        /warp set <warp name>  Add a warp point.
        /warp delete <warp name>  Remove a warp point.
    """.trimIndent()
) {
    companion object {
        private val PRESERVED_KEYWORDS = listOf("list", "set", "delete")
    }

    override fun canRun(caller: Player): Boolean {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(caller.world))) {
            UtilPlayerBase.sendMessage(caller, "&cCannot use warps in lobby!")
            return false
        }
        return true
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.isNullOrEmpty() || args.size > 2) {
            return false
        }
        val data = getMapDataManager().get(caller.world)
        val path = WorldUtils.getWorldRelativePath(caller.world)

        val op = args[0].lowercase()
        val knownWarps = data.warps()

        val site = ConstructionSiteProvider.getSite()
        if (args.size == 1) {
            if (op.equals("list", ignoreCase = true)) {
                if (knownWarps.isEmpty()) {
                    UtilPlayerBase.sendMessage(caller, "&cNo warp point yet! Add some!")
                    return true
                }
                for ((warpKey, warpLocation) in knownWarps) {
                    val message = TextComponent("$warpKey: ${vecToStrClean(warpLocation)}")
                    message.color = ChatColor.YELLOW
                    message.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warp $warpKey")
                    message.hoverEvent = HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        arrayOf(TextComponent("Click to run the command!"))
                    )
                    caller.spigot().sendMessage(message)
                }
                return true
            } else if (PRESERVED_KEYWORDS.contains(op)) {
                return false
            }

            val key = op
            if (!key.matches("^[a-zA-Z][a-zA-Z0-9]+$".toRegex())) {
                UtilPlayerBase.sendMessage(caller, "&cInvalid input.")
                return false
            }
            val location = knownWarps[key] ?: run {
                UtilPlayerBase.sendMessage(caller, "&cUnknown warp point \"$key\"")
                return true
            }
            if (getTeleportManager().teleportPlayer(caller, location.toLocation(caller.world))) {
                UtilPlayerBase.sendMessage(caller, "Teleported to warp point &e$key")
                site.pluginLogger.info("World $path: ${caller.name} teleported to warp point $key (${vecToStrClean(location)})")
            } else {
                sayTeleportFail(caller)
            }
            return true
        }

        val key = args[1].lowercase()
        if (PRESERVED_KEYWORDS.contains(key)) {
            UtilPlayerBase.sendMessage(caller, "&cYou can't use \"list\", \"delete\" or \"set\" as warp point name!")
            return false
        }
        if (!key.matches("^[a-zA-Z][a-zA-Z0-9]+$".toRegex())) {
            UtilPlayerBase.sendMessage(caller, "&cInvalid input.")
            return false
        }
        val warps = data.warps().toMutableMap()
        when (op) {
            "set" -> {
                val loc = caller.location
                warps.putIfAbsent(key, Vector(loc.blockX, loc.blockY, loc.blockZ))?.let {
                    UtilPlayerBase.sendMessage(caller, "&c\"$key\" already exists!")
                    return true
                }
                ConstructionSiteProvider.getScheduler().scheduleAsync {
                    (data as MutableMapData).update(FinalizedMapData(ImmutableMap.copyOf(warps)))
                    UtilPlayerBase.sendMessage(caller, "Created warp point &e$key")
                    site.pluginLogger.info("World $path: ${caller.name} created warp point $key at ${locToStrClean(caller.location)}")
                }
                return true
            }

            "delete" -> {
                if (!warps.containsKey(key)) {
                    UtilPlayerBase.sendMessage(caller, "&c\"$key\" does not exist!")
                    return true
                }
                val location = warps.remove(key)
                ConstructionSiteProvider.getScheduler().scheduleAsync {
                    UtilPlayerBase.sendMessage(caller, "Deleting warp point &e$key")
                    (data as MutableMapData).update(FinalizedMapData(ImmutableMap.copyOf(warps)))
                    site.pluginLogger.info("World $path: ${caller.name} deleted warp point $key (${vecToStrClean(location)})")
                }
                return true
            }

            else -> {
                return false
            }
        }
    }
}
