package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.GameTypeUtils
import colosseum.construction.manager.GameTypeInfoManager
import colosseum.utility.GameTypeInfo
import colosseum.utility.UtilPlayerBase
import colosseum.utility.arcade.GameType
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class GameTypeInfoCommand: ConstructionSiteCommand(
    listOf("gametypeinfo"),
    "Get/Set GameType information.",
    """
        /gametypeinfo <gametype>  Get info of a GameType
        /gametypeinfo add <gametype> <info>  Add a line of GameType info
        /gametypeinfo delete <gametype> <line>  Delete a line of GameType info
        /gametypeinfo clear <gametype>  Clear info of a GameType
    """.trimIndent()
), TabCompleter {
    private fun getGameTypeInfoManager(): GameTypeInfoManager {
        return ConstructionSiteProvider.getSite().getManager(GameTypeInfoManager::class.java)
    }

    private fun content(args: Array<String>): StringBuilder {
        return StringBuilder(args.drop(2).joinToString(" "))
    }

    private fun clearGameTypeInfo(gameType: GameType) {
        val manager = getGameTypeInfoManager()
        val obj = manager.getGameTypeInfo(gameType)
        if (obj != null) {
            obj.info.clear()
            manager.setGameTypeInfo(gameType, obj)
        }
    }

    private fun deleteGameTypeInfoByLineNumber(gameType: GameType, lineNumber: Int) {
        val manager = getGameTypeInfoManager()
        val obj = manager.getGameTypeInfo(gameType)
        if (obj != null) {
            obj.removeInfo(lineNumber - 1)
            manager.setGameTypeInfo(gameType, obj)
        }
    }

    private fun appendGameTypeInfo(gameType: GameType, content: String) {
        val manager = getGameTypeInfoManager()
        val obj = manager.getGameTypeInfo(gameType) ?: GameTypeInfo(gameType, ArrayList())
        manager.setGameTypeInfo(gameType, obj)
        obj.addInfo(content)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        if (args.size >= 3 || args.isNullOrEmpty()) {
            return null
        }
        val s: MutableList<String> = ArrayList()
        s.addAll(GameTypeUtils.getGameTypes().map { v -> v.name })
        if (args.size == 1) {
            s.addAll(listOf("add", "delete", "clear"))
        }
        return StringUtil.copyPartialMatches(args[args.size - 1], s, ArrayList<String>())
    }

    override fun canRun(console: CommandSender): Boolean {
        return ConstructionSiteProvider.isLive()
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        return runConstruction(caller as CommandSender, label, args)
    }

    override fun runConstruction(caller: CommandSender, label: String, args: Array<String>): Boolean {
        when (args.size) {
            1 -> {
                val gameRaw = args[0]
                val gameType = GameTypeUtils.determineGameType(gameRaw, true)
                if (gameType == GameType.None) {
                    GameTypeUtils.printValidGameTypes(caller)
                    return true
                }
                val info = getGameTypeInfoManager().getGameTypeInfo(gameType)
                if (info == null || info.info.isNullOrEmpty()) {
                    UtilPlayerBase.sendMessage(caller, "&cNo info found for &e${gameType.name}")
                    return true
                }
                for (s in info.info.iterator()) {
                    UtilPlayerBase.sendMessage(caller, s)
                }
                return true
            }

            2 -> {
                if (!args[0].equals("clear", ignoreCase = true)) {
                    return false
                }
                val gameRaw = args[1]
                val gameType = GameTypeUtils.determineGameType(gameRaw, true)
                if (gameType == GameType.None) {
                    GameTypeUtils.printValidGameTypes(caller)
                    return true
                }
                clearGameTypeInfo(gameType)
                Command.broadcastCommandMessage(caller, "Clear gametype info for ${gameType.name}", true)
                return true
            }
        }
        if (args.size >= 3) {
            val op = args[0].lowercase()
            when (op) {
                "add" -> {
                    val gameRaw = args[1]
                    val gameType = GameTypeUtils.determineGameType(gameRaw, true)
                    if (gameType == GameType.None) {
                        GameTypeUtils.printValidGameTypes(caller)
                        return true
                    }
                    val content = content(args).toString()
                    appendGameTypeInfo(gameType, content)
                    Command.broadcastCommandMessage(caller, "Add new gametype info content to ${gameType.name}: $content", true)
                    return true
                }

                "delete" -> {
                    try {
                        if (args.size != 3) {
                            throw ArrayIndexOutOfBoundsException()
                        }
                        val gameRaw = args[1]
                        val gameType = GameTypeUtils.determineGameType(gameRaw, true)
                        if (gameType == GameType.None) {
                            GameTypeUtils.printValidGameTypes(caller)
                            return true
                        }
                        val lineNumber = args[2].toInt()
                        if (lineNumber < 1) {
                            throw NumberFormatException()
                        }
                        deleteGameTypeInfoByLineNumber(gameType, lineNumber)
                        Command.broadcastCommandMessage(caller, "Remove gametype info from ${gameType.name} at line $lineNumber", true)
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }

                else -> {
                    return false
                }
            }
        } else {
            return false
        }
    }
}
