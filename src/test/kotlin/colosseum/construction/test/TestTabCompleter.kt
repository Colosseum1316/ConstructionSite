package colosseum.construction.test

import colosseum.construction.command.NewMapCommand
import colosseum.construction.command.vanilla.DifficultyCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestTabCompleter {

    @Test
    fun testNewMapCommand() {
        val command = NewMapCommand()
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf()))
        Assertions.assertNull(command.onTabComplete(null, null, command.aliases[0], arrayOf("", "")))

        Assertions.assertEquals(3, command.onTabComplete(null, null, command.aliases[0], arrayOf(""))!!.size)
        Assertions.assertEquals(3, command.onTabComplete(null, null, command.aliases[0], arrayOf("-"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf("-v"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf("-n"))!!.size)
        Assertions.assertEquals(1, command.onTabComplete(null, null, command.aliases[0], arrayOf("-e"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("-vne"))!!.size)
        Assertions.assertEquals(0, command.onTabComplete(null, null, command.aliases[0], arrayOf("-a"))!!.size)
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