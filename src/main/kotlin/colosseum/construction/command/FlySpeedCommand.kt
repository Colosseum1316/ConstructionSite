package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

class FlySpeedCommand: ConstructionSiteCommand(
    listOf("flyspeed"),
    "Set your flight speed. Use it whilst flying. Range from 1.0 to 10.0",
    "/flyspeed <speed>"
) {
    override fun canRun(caller: Player): Boolean {
        return super.canRun(caller) && caller.isFlying
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        val newSpeed: Double

        if (args.size == 1) {
            try {
                newSpeed = args[0].toDouble()
                if (newSpeed !in 1.0..10.0) {
                    return false
                }
            } catch (ex: NumberFormatException) {
                return false
            }
        } else {
            return false
        }

        caller.flySpeed = (newSpeed / 10.0).toFloat()
        UtilPlayerBase.sendMessage(caller, "Set your flight speed to &e${caller.flySpeed}")
        ConstructionSiteProvider.getSite().pluginLogger.info("Set flight speed to ${caller.flySpeed} for ${caller.name}")
        return true
    }
}
