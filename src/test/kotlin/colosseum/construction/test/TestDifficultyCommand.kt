package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.command.vanilla.DifficultyCommand
import colosseum.construction.manager.TeleportManager
import colosseum.construction.test.dummies.ConstructionSitePlayerMock
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.WorldMapConstants
import org.bukkit.Difficulty
import org.bukkit.Location
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

internal class TestDifficultyCommand {
    companion object {
        @TempDir
        @JvmField
        var tempWorldContainer: File? = null

        @TempDir
        @JvmField
        var tempPluginDataDir: File? = null

        private const val uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf"
        private const val uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693"
    }

    private var plugin: DummySite? = null
    private lateinit var world: ConstructionSiteWorldMock
    private lateinit var worldLobby: ConstructionSiteWorldMock
    private lateinit var worldMap: ConstructionSiteWorldMock

    private lateinit var player1: ConstructionSitePlayerMock
    private lateinit var player2: ConstructionSitePlayerMock

    @BeforeAll
    fun setup() {
        tearDown()

        plugin = DummySite3(tempWorldContainer, tempPluginDataDir)

        world = ConstructionSiteWorldMock(WorldMapConstants.WORLD)
        worldLobby = ConstructionSiteWorldMock(WorldMapConstants.WORLD_LOBBY)
        worldMap = ConstructionSiteWorldMock("test_map", true)
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(world)
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(worldLobby)
        Assertions.assertEquals(worldLobby, MockBukkit.getMock().getWorld(WorldMapConstants.WORLD_LOBBY))
        Assertions.assertEquals(world, MockBukkit.getMock().getWorld(WorldMapConstants.WORLD))

        player1 = ConstructionSitePlayerMock("test1", UUID.fromString(uuid1))
        player1.isOp = false
        (MockBukkit.getMock() as ConstructionSiteServerMock).addPlayer(player1)
        player2 = ConstructionSitePlayerMock("test2", UUID.fromString(uuid2))
        player2.isOp = false
        (MockBukkit.getMock() as ConstructionSiteServerMock).addPlayer(player2)

        plugin!!.load()
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(worldMap)
        Assertions.assertEquals(worldMap, MockBukkit.getMock().getWorld(WorldUtils.getWorldRelativePath(worldMap)))
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs())
        Utils.writeMapData(
            WorldUtils.getWorldFolder(worldMap), String.format(
                """
                currentlyLive:true
                warps:
                MAP_NAME:Test map3 difficulty
                MAP_AUTHOR:Test author4
                GAME_TYPE:None
                ADMIN_LIST:%s
                """.trimIndent().trim { it <= ' ' }, uuid2
            )
        )
        plugin!!.enable()

        world.difficulty = Difficulty.PEACEFUL
        worldMap.difficulty = Difficulty.PEACEFUL
        worldLobby.difficulty = Difficulty.PEACEFUL
    }

    @AfterAll
    fun tearDown() {
        Utils.tearDown(plugin)
    }

    @Order(1)
    @Test
    fun testPermission() {
        val command = DifficultyCommand()
        val manager: TeleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)

        Assertions.assertTrue(manager.teleportToServerSpawn(player1))
        Assertions.assertTrue(manager.teleportToServerSpawn(player2))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(command.canRun(player1))
        player1.assertSaid("§cYou are in \"world\"!")
        player1.assertNoMoreSaid()
        Assertions.assertFalse(command.canRun(player2))
        player2.assertSaid("§cYou are in \"world\"!")
        player2.assertNoMoreSaid()

        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(command.canRun(player1))
        player1.assertSaid("§cYou are in \"world_lobby\"!")
        player1.assertNoMoreSaid()
        Assertions.assertFalse(command.canRun(player2))
        player2.assertSaid("§cYou are in \"world_lobby\"!")
        player2.assertNoMoreSaid()

        Assertions.assertFalse(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(command.canRun(player1))
        player1.assertSaid("§cYou are in \"world_lobby\"!")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.canRun(player2))
        player2.assertNoMoreSaid()

        player1.isOp = true
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertTrue(command.canRun(player1))
        player1.assertNoMoreSaid()
        player1.isOp = false
    }

    @Order(2)
    @Test
    fun testNumericDifficulties() {
        val command = DifficultyCommand()
        val label = command.aliases[0]
        Assertions.assertFalse(command.runConstruction(player2, label, arrayOf()))
        Assertions.assertFalse(command.runConstruction(player2, label, arrayOf("1", "1")))
        Assertions.assertFalse(command.runConstruction(player2, label, arrayOf("9")))
        Assertions.assertFalse(command.runConstruction(player2, label, arrayOf("-1")))

        world.difficulty = Difficulty.NORMAL
        worldMap.difficulty = Difficulty.NORMAL
        worldLobby.difficulty = Difficulty.NORMAL

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("0")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to PEACEFUL")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.PEACEFUL, world.difficulty)
        Assertions.assertNotEquals(Difficulty.PEACEFUL, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.PEACEFUL, worldMap.difficulty)

        world.difficulty = Difficulty.PEACEFUL
        worldMap.difficulty = Difficulty.PEACEFUL
        worldLobby.difficulty = Difficulty.PEACEFUL

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("1")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to EASY")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.EASY, world.difficulty)
        Assertions.assertNotEquals(Difficulty.EASY, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.EASY, worldMap.difficulty)

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("2")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to NORMAL")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.NORMAL, world.difficulty)
        Assertions.assertNotEquals(Difficulty.NORMAL, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.NORMAL, worldMap.difficulty)

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("3")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to HARD")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.HARD, world.difficulty)
        Assertions.assertNotEquals(Difficulty.HARD, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.HARD, worldMap.difficulty)
    }

    @Order(3)
    @Test
    fun testDifficultiesByName() {
        val command = DifficultyCommand()
        val label = command.aliases[0]

        world.difficulty = Difficulty.NORMAL
        worldMap.difficulty = Difficulty.NORMAL
        worldLobby.difficulty = Difficulty.NORMAL

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("peaceFUL")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to PEACEFUL")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.PEACEFUL, world.difficulty)
        Assertions.assertNotEquals(Difficulty.PEACEFUL, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.PEACEFUL, worldMap.difficulty)

        world.difficulty = Difficulty.PEACEFUL
        worldMap.difficulty = Difficulty.PEACEFUL
        worldLobby.difficulty = Difficulty.PEACEFUL

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("Easy")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to EASY")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.EASY, world.difficulty)
        Assertions.assertNotEquals(Difficulty.EASY, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.EASY, worldMap.difficulty)

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("normal")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to NORMAL")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.NORMAL, world.difficulty)
        Assertions.assertNotEquals(Difficulty.NORMAL, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.NORMAL, worldMap.difficulty)

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("HaRd")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to HARD")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.HARD, world.difficulty)
        Assertions.assertNotEquals(Difficulty.HARD, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.HARD, worldMap.difficulty)
    }

    @Order(4)
    @Test
    fun testDifficultiesByPrefix() {
        val command = DifficultyCommand()
        val label = command.aliases[0]

        world.difficulty = Difficulty.NORMAL
        worldMap.difficulty = Difficulty.NORMAL
        worldLobby.difficulty = Difficulty.NORMAL

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("p")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to PEACEFUL")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.PEACEFUL, world.difficulty)
        Assertions.assertNotEquals(Difficulty.PEACEFUL, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.PEACEFUL, worldMap.difficulty)

        world.difficulty = Difficulty.PEACEFUL
        worldMap.difficulty = Difficulty.PEACEFUL
        worldLobby.difficulty = Difficulty.PEACEFUL

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("E")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to EASY")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.EASY, world.difficulty)
        Assertions.assertNotEquals(Difficulty.EASY, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.EASY, worldMap.difficulty)

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("n")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to NORMAL")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.NORMAL, world.difficulty)
        Assertions.assertNotEquals(Difficulty.NORMAL, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.NORMAL, worldMap.difficulty)

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("H")))
        player2.assertSaid("Set map Test map3 difficulty world difficulty to HARD")
        player2.assertNoMoreSaid()
        Assertions.assertNotEquals(Difficulty.HARD, world.difficulty)
        Assertions.assertNotEquals(Difficulty.HARD, worldLobby.difficulty)
        Assertions.assertEquals(Difficulty.HARD, worldMap.difficulty)
    }
}
