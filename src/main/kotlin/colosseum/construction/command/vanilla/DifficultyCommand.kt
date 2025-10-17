package colosseum.construction.command.vanilla

import colosseum.construction.command.AbstractMapAdminCommand
import org.bukkit.Difficulty
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.command.defaults.DifficultyCommand
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.lang.reflect.Field
import java.lang.reflect.Method

class DifficultyCommand: AbstractMapAdminCommand(
    listOf("difficulty"),
    "Set map world difficulty",
    "/difficulty <new difficulty>"
), TabCompleter {
    companion object {
        private val vanilla: DifficultyCommand = DifficultyCommand()
        private val method: Method = vanilla.javaClass.getDeclaredMethod("getDifficultyForString", CommandSender::class.java, String::class.java)
        private val field: Field = vanilla.javaClass.getDeclaredField("DIFFICULTY_NAMES")

        init {
            method.isAccessible = true
            field.isAccessible = true
        }
    }

    override fun onTabComplete(
        sender: CommandSender?,
        command: Command?,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size == 1) {
            return StringUtil.copyPartialMatches(args[0], field.get(null) as List<String>, ArrayList())
        }
        return null
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.size != 1) {
            return false
        }

        try {
            val v = args[0].toInt()
            if (v < 0 || v > 3) {
                return false
            }
            val difficulty = Difficulty.getByValue(v)
            setDifficulty(caller, caller.world, difficulty)
            return true
        } catch (e: Exception) {
            // no op
        }

        val difficulty = Difficulty.getByValue(method.invoke(vanilla, caller, args[0]) as Int) ?: return false
        setDifficulty(caller, caller.world, difficulty)
        return true
    }

    private fun setDifficulty(caller: Player, world: World, difficulty: Difficulty) {
        world.difficulty = difficulty
        Command.broadcastCommandMessage(caller, "Set map ${getMapDataManager().get(world).mapName} world difficulty to $difficulty", true)
    }
}