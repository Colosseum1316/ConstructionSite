package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.command.MapAdminCommand
import colosseum.construction.data.MapData
import colosseum.construction.data.MutableMapData
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import colosseum.construction.test.dummies.ConstructionSitePlayerMock
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.MapConstants
import org.bukkit.Location
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

internal class TestMapAdminCommand {
    companion object {
        private const val uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf"
        private const val uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693"

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
    private lateinit var world: ConstructionSiteWorldMock
    private lateinit var worldLobby: ConstructionSiteWorldMock
    private lateinit var worldMap: ConstructionSiteWorldMock

    @BeforeAll
    fun setup() {
        tearDown()

        plugin = DummySite3(tempWorldContainer, tempPluginDataDir)

        world = ConstructionSiteWorldMock(MapConstants.WORLD)
        worldLobby = ConstructionSiteWorldMock(MapConstants.WORLD_LOBBY)
        worldMap = ConstructionSiteWorldMock("test_map", true)
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(world)
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(worldLobby)
        Assertions.assertEquals(worldLobby, MockBukkit.getMock().getWorld(MapConstants.WORLD_LOBBY))
        Assertions.assertEquals(world, MockBukkit.getMock().getWorld(MapConstants.WORLD))

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
                MAP_NAME:Test mapadmin
                MAP_AUTHOR:Test mapadmin
                ADMIN_LIST:%s
                """.trimIndent().trim { it <= ' ' }, uuid1
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
        val command = MapAdminCommand()
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        Assertions.assertTrue(manager.teleportToServerSpawn(player1))
        Assertions.assertTrue(manager.teleportToServerSpawn(player2))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(command.canRun(player1))
        Assertions.assertFalse(command.canRun(player2))
        player1.assertSaid("§cYou are in \"world\"!")
        player1.assertNoMoreSaid()
        player2.assertSaid("§cYou are in \"world\"!")
        player2.assertNoMoreSaid()
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(command.canRun(player1))
        Assertions.assertFalse(command.canRun(player2))
        player1.assertSaid("§cYou are in \"world_lobby\"!")
        player1.assertNoMoreSaid()
        player2.assertSaid("§cYou are in \"world_lobby\"!")
        player2.assertNoMoreSaid()
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(manager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertTrue(command.canRun(player1))
        Assertions.assertFalse(command.canRun(player2))
        player1.assertNoMoreSaid()
        player2.assertSaid("§cYou are in \"world_lobby\"!")
        player2.assertNoMoreSaid()
    }

    @Order(2)
    @Test
    fun test() {
        val command = MapAdminCommand()
        val label: String = command.aliases[0]
        val teleportManager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        val mapDataManager: MapDataManager =
            ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf()))
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("", "")))
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("t")))
        Assertions.assertNotNull(player1.nextMessage())
        player1.assertNoMoreSaid()
        Assertions.assertTrue(
            mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid1))
        )
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player1))
        Assertions.assertFalse(
            mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid2))
        )
        Assertions.assertFalse(mapDataManager.get(worldMap).allows(player2))
        var data: MapData = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertNotNull(data)
        Assertions.assertTrue(data.allows(player1))
        Assertions.assertFalse(data.allows(player2))
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid1)))
        Assertions.assertFalse(data.adminList().contains(UUID.fromString(uuid2)))

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("test2")))
        player1.assertSaid("test2 is now admin in Test mapadmin")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(
            mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid1))
        )
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player1))
        Assertions.assertTrue(
            mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid2))
        )
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player2))
        Assertions.assertTrue((mapDataManager.get(worldMap) as MutableMapData).write())
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertTrue(data.allows(player1))
        Assertions.assertTrue(data.allows(player2))
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid1)))
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid2)))

        Assertions.assertTrue(teleportManager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("test1")))
        player2.assertSaid("test1 is no longer admin in Test mapadmin")
        player2.assertNoMoreSaid()
        Assertions.assertFalse(
            mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid1))
        )
        Assertions.assertFalse(mapDataManager.get(worldMap).allows(player1))
        Assertions.assertTrue(
            mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid2))
        )
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player2))
        Assertions.assertTrue((mapDataManager.get(worldMap) as MutableMapData).write())
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertFalse(data.allows(player1))
        Assertions.assertTrue(data.allows(player2))
        Assertions.assertFalse(data.adminList().contains(UUID.fromString(uuid1)))
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid2)))
    }
}
