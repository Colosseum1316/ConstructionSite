package colosseum.construction.test

import colosseum.construction.GameTypeUtils
import colosseum.construction.command.GameTypeInfoCommand
import colosseum.construction.command.MapGameTypeCommand
import colosseum.construction.command.NewMapCommand
import colosseum.construction.command.vanilla.DifficultyCommand
import colosseum.utility.arcade.GameType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestTabCompleter {
    @Test
    fun testGameTypeInfoCommand() {
        val command = GameTypeInfoCommand()
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf()))
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf("", "", "")))
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf("", "", "", "")))
        Assertions.assertEquals(GameTypeUtils.getGameTypes().size + 3, command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.size)
        Assertions.assertFalse(command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.contains(GameType.None.name))
        Assertions.assertTrue(command.onTabComplete(null, null, command.aliases[0], arrayOf("A"))!!.isNotEmpty())
        Assertions.assertTrue(command.onTabComplete(null, null, command.aliases[0], arrayOf("c"))!!.isNotEmpty())
        Assertions.assertTrue(command.onTabComplete(null, null, command.aliases[0], arrayOf("delete"))!!.size == 1)
        Assertions.assertEquals(GameTypeUtils.getGameTypes().size, command.onTabComplete(null, null, command.aliases[0], arrayOf("add", ""))!!.size)
        Assertions.assertFalse(command.onTabComplete(null, null, command.aliases[0], arrayOf("add", ""))!!.contains(GameType.None.name))
    }

    @Test
    fun testMapGameTypeCommand() {
        val command = MapGameTypeCommand()
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf()))
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf("", "", "")))
        Assertions.assertEquals(GameTypeUtils.getGameTypes().size, command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("abc123"))!!.size)
        Assertions.assertFalse(command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.contains(GameType.None.name))
    }

    @Test
    fun testNewMapCommand() {
        val command = NewMapCommand()
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf()))
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf("", "", "")))
        Assertions.assertEquals(GameTypeUtils.getGameTypes().size, command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("abc123"))!!.size)
        Assertions.assertFalse(command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.contains(GameType.None.name))

        Assertions.assertEquals(3, command.onTabComplete(null, null, command.aliases[0], arrayOf(GameTypeUtils.getGameTypes()[0].name, ""))!!.size)
        Assertions.assertEquals(3, command.onTabComplete(null, null, command.aliases[0], arrayOf(GameTypeUtils.getGameTypes()[0].name, "-"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf(GameTypeUtils.getGameTypes()[0].name, "-v"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf(GameTypeUtils.getGameTypes()[0].name, "-n"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf(GameTypeUtils.getGameTypes()[0].name, "-e"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf(GameTypeUtils.getGameTypes()[0].name, "-vne"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf(GameTypeUtils.getGameTypes()[0].name, "-a"))!!.size)
    }

    @Test
    fun testDifficultyCommand() {
        val command = DifficultyCommand()
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf()))
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf("", "")))
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("b"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf("p"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf("E"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf("Normal"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf("Ha"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("Non"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("0"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("1"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("2"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("3"))!!.size)
        Assertions.assertEquals(4, command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.size)
    }
}