package colosseum.construction.command.vanilla

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.command.AbstractTeleportCommand
import colosseum.utility.UtilPlayerBase
import colosseum.utility.UtilPlayerBase.searchOnline
import colosseum.utility.UtilWorld.locToStrClean
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.TeleportCommand
import org.bukkit.entity.Player
import java.lang.reflect.Method

@Suppress("deprecation", "RedundantSuppression")
class TeleportCommand: AbstractTeleportCommand(
    listOf("tp", "teleport"),
    "Teleport.",
    """
        /tp <player>  Teleport to another player
        /tp <from> <to>  Teleport from player to another player
        /tp <x> <y> <z>  Teleport to coordinates"
    """.trimIndent()
) {
    companion object {
        private val vanilla: TeleportCommand = TeleportCommand()
        private val getCoordinate: Method = vanilla.javaClass.getDeclaredMethod("getCoordinate", CommandSender::class.java, Double::class.java, String::class.java)
        private val getCoordinateWithMinMax: Method = vanilla.javaClass.getDeclaredMethod("getCoordinate", CommandSender::class.java, Double::class.java, String::class.java, Int::class.java, Int::class.java)

        init {
            getCoordinate.isAccessible = true
            getCoordinateWithMinMax.isAccessible = true
        }
    }

    private fun teleportSelf(caller: Player, target: Player) {
        if (target == caller) {
            return
        }
        if (getTeleportManager().teleportPlayer(caller, target)) {
            UtilPlayerBase.sendMessage(caller, "You teleported to &e${target.name}")
            ConstructionSiteProvider.getSite().pluginLogger.info("${caller.name} teleported to ${target.name}")
        } else {
            sayTeleportFail(caller)
        }
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        when (args.size) {
            1 -> {
                val target = searchOnline(caller, args[0], true) ?: return true  // Player has already been informed
                teleportSelf(caller, target)
            }
            2 -> {
                // Already informed
                val from = searchOnline(caller, args[0], true) ?: return true
                // They must be OP to teleport a player to another player
                if (!caller.isOp && from != caller) {
                    sayTeleportFail(caller)
                    return true
                }
                val to = searchOnline(caller, args[1], true) ?: return true
                if (from == caller) {
                    teleportSelf(caller, to)
                } else {
                    if (getTeleportManager().teleportPlayer(from, to)) {
                        if (to != caller) {
                            UtilPlayerBase.sendMessage(caller, "You teleported &e${from.name}&r to &e${to.name}")
                            UtilPlayerBase.sendMessage(from, "&e${caller.name}&r teleported you to &e${to.name}")
                        } else {
                            UtilPlayerBase.sendMessage(caller, "You teleported &e${from.name}&r to you")
                            UtilPlayerBase.sendMessage(from, "You are teleported to &e${to.name}")
                        }
                        ConstructionSiteProvider.getSite().pluginLogger.info("${caller.name} teleported ${from.name} to ${to.name}")
                    } else {
                        sayTeleportFail(caller)
                    }
                }
            }
            3 -> {
                val x: Double = getCoordinate.invoke(vanilla, caller, caller.location.x, args[0]) as Double
                val y: Double = getCoordinateWithMinMax.invoke(vanilla, caller, caller.location.y, args[1], 0, 0) as Double
                val z: Double = getCoordinate.invoke(vanilla, caller, caller.location.z, args[2]) as Double
                val destination = caller.location.clone()
                destination.x = x
                destination.y = y
                destination.z = z
                if (getTeleportManager().teleportPlayer(caller, destination)) {
                    UtilPlayerBase.sendMessage(caller, "You teleported to &e${locToStrClean(destination)}")
                } else {
                    sayTeleportFail(caller)
                }
            }
            else -> {
                return false
            }
        }
        return true
    }
}
