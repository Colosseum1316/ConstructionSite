package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.command.AbstractMapCreditCommand
import colosseum.construction.command.MapAuthorCommand
import colosseum.construction.command.MapNameCommand
import colosseum.construction.data.MapData
import colosseum.construction.data.MutableMapData
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import colosseum.construction.test.dummies.ConstructionSitePlayerMock
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.WorldMapConstants
import org.bukkit.Location
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

internal class TestMapCreditCommands {
    companion object {
        private const val uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf"
        private const val uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693"
        private const val uuid3 = "3e65ea50-cd1a-45fb-81d7-7e27c14662d4"

        @TempDir
        @JvmField
        var tempWorldContainer: File? = null

        @TempDir
        @JvmField
        var tempPluginDataDir: File? = null
    }
    
    private var plugin: DummySite? = null
    private lateinit var player1: ConstructionSitePlayerMock
    private lateinit var player2: ConstructionSitePlayerMock
    private lateinit var player3: ConstructionSitePlayerMock
    private lateinit var world: ConstructionSiteWorldMock
    private lateinit var worldLobby: ConstructionSiteWorldMock
    private lateinit var worldMap: ConstructionSiteWorldMock

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
        player1.isOp = true
        (MockBukkit.getMock() as ConstructionSiteServerMock).addPlayer(player1)
        player2 = ConstructionSitePlayerMock("test2", UUID.fromString(uuid2))
        player2.isOp = false
        (MockBukkit.getMock() as ConstructionSiteServerMock).addPlayer(player2)
        player3 = ConstructionSitePlayerMock("test3", UUID.fromString(uuid3))
        player3.isOp = false
        (MockBukkit.getMock() as ConstructionSiteServerMock).addPlayer(player3)

        plugin!!.load()
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(worldMap)
        Assertions.assertEquals(worldMap, MockBukkit.getMock().getWorld(WorldUtils.getWorldRelativePath(worldMap)))
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs())
        Utils.writeMapData(
            WorldUtils.getWorldFolder(worldMap), String.format(
                """
                currentlyLive:true
                warps:
                MAP_NAME:map none
                MAP_AUTHOR:author none
                ADMIN_LIST:%s
                """.trimIndent().trim { it <= ' ' }, uuid2
            )
        )
        plugin!!.enable()
    }

    @AfterAll
    fun tearDown() {
        Utils.tearDown(plugin)
    }

    @Order(1)
    @Test
    fun testPermission() {
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        Assertions.assertTrue(manager.teleportToServerSpawn(player1))
        Assertions.assertTrue(manager.teleportToServerSpawn(player2))
        Assertions.assertTrue(manager.teleportToServerSpawn(player3))

        val commands: Array<AbstractMapCreditCommand> = arrayOf(
            MapAuthorCommand(),
            MapNameCommand(),
        )

        for (command in commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
            Assertions.assertFalse(command.canRun(player1))
            Assertions.assertFalse(command.canRun(player2))
            Assertions.assertFalse(command.canRun(player3))
            player1.assertSaid("§cYou are in \"world\"!")
            player1.assertNoMoreSaid()
            player2.assertSaid("§cYou are in \"world\"!")
            player2.assertNoMoreSaid()
            player3.assertSaid("§cYou are in \"world\"!")
            player3.assertNoMoreSaid()
        }

        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player3, Location(worldLobby, 0.0, 0.0, 0.0)))

        for (command in commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
            Assertions.assertFalse(command.canRun(player1))
            Assertions.assertFalse(command.canRun(player2))
            Assertions.assertFalse(command.canRun(player3))
            player1.assertSaid("§cYou are in \"world_lobby\"!")
            player1.assertNoMoreSaid()
            player2.assertSaid("§cYou are in \"world_lobby\"!")
            player2.assertNoMoreSaid()
            player3.assertSaid("§cYou are in \"world_lobby\"!")
            player3.assertNoMoreSaid()
        }

        Assertions.assertTrue(manager.canTeleportTo(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.canTeleportTo(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(manager.canTeleportTo(player3, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(manager.teleportPlayer(player3, Location(worldMap, 0.0, 0.0, 0.0)))

        for (command in commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
            Assertions.assertTrue(command.canRun(player1))
            Assertions.assertTrue(command.canRun(player2))
            Assertions.assertFalse(command.canRun(player3))
            player3.assertSaid("§cYou are in \"world_lobby\"!")
            player3.assertNoMoreSaid()
            player1.assertNoMoreSaid()
            player2.assertNoMoreSaid()
        }
    }

    @Order(2)
    @Test
    fun testMapAuthorCommand() {
        val command: AbstractMapCreditCommand = MapAuthorCommand()
        val label: String = command.aliases[0]
        val teleportManager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        val mapDataManager: MapDataManager =
            ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(teleportManager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        player1.assertLocation(Location(worldMap, 0.0, 0.0, 0.0), 1.0)
        player2.assertLocation(Location(worldMap, 0.0, 0.0, 0.0), 1.0)
        player1.assertNoMoreSaid()
        player2.assertNoMoreSaid()

        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf()))
        Assertions.assertFalse(command.runConstruction(player2, label, arrayOf()))

        Assertions.assertTrue(
            command.runConstruction(
                player1,
                label,
                arrayOf("It's", "player", "1")
            )
        )
        player1.assertSaid("Set author: It's player 1")
        player1.assertNoMoreSaid()
        player2.assertNoMoreSaid()
        Assertions.assertEquals("It's player 1", mapDataManager.get(worldMap).mapCreator)
        Assertions.assertTrue((mapDataManager.get(worldMap) as MutableMapData).write())
        var data: MapData = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertEquals("It's player 1", data.mapCreator)

        Assertions.assertTrue(
            command.runConstruction(
                player2,
                label,
                arrayOf("This", "is", "player", "2")
            )
        )
        player2.assertSaid("Set author: This is player 2")
        player2.assertNoMoreSaid()
        player1.assertNoMoreSaid()
        Assertions.assertEquals("This is player 2", mapDataManager.get(worldMap).mapCreator)
        Assertions.assertTrue((mapDataManager.get(worldMap) as MutableMapData).write())
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertEquals("This is player 2", data.getMapCreator())
    }

    @Order(3)
    @Test
    fun testMapNameCommand() {
        val command: AbstractMapCreditCommand = MapNameCommand()
        val label: String = command.aliases.get(0)
        val teleportManager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        val mapDataManager: MapDataManager =
            ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(teleportManager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        player1.assertLocation(Location(worldMap, 0.0, 0.0, 0.0), 1.0)
        player2.assertLocation(Location(worldMap, 0.0, 0.0, 0.0), 1.0)
        player1.assertNoMoreSaid()
        player2.assertNoMoreSaid()

        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf()))
        Assertions.assertFalse(command.runConstruction(player2, label, arrayOf()))

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("Map", "v1")))
        player1.assertSaid("Set map name: Map v1")
        player1.assertNoMoreSaid()
        player2.assertNoMoreSaid()
        Assertions.assertEquals("Map v1", mapDataManager.get(worldMap).mapName)
        Assertions.assertTrue((mapDataManager.get(worldMap) as MutableMapData).write())
        var data: MapData = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertEquals("Map v1", data.mapName)

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("Map", "v2")))
        player2.assertSaid("Set map name: Map v2")
        player2.assertNoMoreSaid()
        player1.assertNoMoreSaid()
        Assertions.assertEquals("Map v2", mapDataManager.get(worldMap).mapName)
        Assertions.assertTrue((mapDataManager.get(worldMap) as MutableMapData).write())
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertEquals("Map v2", data.getMapName())
    }
}
