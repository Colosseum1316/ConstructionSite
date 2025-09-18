package colosseum.construction.command.vanilla

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.command.AbstractTeleportCommand
import colosseum.utility.UtilPlayerBase
import colosseum.utility.UtilPlayerBase.searchOnline
import colosseum.utility.UtilWorld.locToStrClean
import org.bukkit.entity.Player

class TeleportCommand: AbstractTeleportCommand(
    listOf("tp", "teleport"),
    "Teleport.",
    """
        /tp <player>  Teleport to another player
        /tp <from> <to>  Teleport from player to another player
        /tp <x> <y> <z>  Teleport to coordinates"
    """.trimIndent()
) {
    private fun teleportSelf(caller: Player, target: Player) {
        if (target == caller) {
            return
        }
        if (getTeleportManager().teleportPlayer(caller, target)) {
            UtilPlayerBase.sendMessage(caller, "You teleported to &e${target.name}")
            ConstructionSiteProvider.getSite().pluginLogger.info("${caller.name} teleported to ${target.name}.")
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
                            UtilPlayerBase.sendMessage(caller, "You teleported &e${from.name}&r to &e${to.name}.")
                            UtilPlayerBase.sendMessage(from, "&e${caller.name}&r teleported you to &e${to.name}.")
                        } else {
                            UtilPlayerBase.sendMessage(caller, "You teleported &e${from.name}&r to you.")
                            UtilPlayerBase.sendMessage(from, "You were teleported to &e${to.name}&r.")
                        }
                        ConstructionSiteProvider.getSite().pluginLogger.info("${caller.name} teleported ${from.name} to ${to.name}.")
                    } else {
                        sayTeleportFail(caller)
                    }
                }
            }
            3 -> {
                val coordinates: MutableList<Double?> = ArrayList()
                coordinates.add(parseCoordinate(args[0], caller.location.x))
                coordinates.add(parseCoordinate(args[1], caller.location.y))
                coordinates.add(parseCoordinate(args[2], caller.location.z))
                for (coordinate in coordinates) {
                    if (coordinate == null) {
                        return false
                    }
                }
                val destination = caller.location.clone()
                destination.x = coordinates[0]!!
                destination.y = coordinates[1]!!
                destination.z = coordinates[2]!!
                if (getTeleportManager().teleportPlayer(caller, destination)) {
                    UtilPlayerBase.sendMessage(caller, "You teleported to &e${locToStrClean(destination)}&r")
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

    private fun parseCoordinate(value: String, beginning: Double): Double? {
        try {
            if (value.startsWith("~")) {
                if (value.length == 1) {
                    return beginning
                }
                val relativeIn = value.substring(1)
                val relative = relativeIn.toDouble()
                return beginning + relative
            }
            return value.toDouble()
        } catch (ex: NumberFormatException) {
            return null
        }
    }
}
