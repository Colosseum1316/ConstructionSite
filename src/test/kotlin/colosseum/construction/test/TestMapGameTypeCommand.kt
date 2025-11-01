package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.command.MapGameTypeCommand
import colosseum.construction.data.MutableMapData
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import colosseum.construction.test.dummies.ConstructionSitePlayerMock
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.MapData
import colosseum.utility.WorldMapConstants
import colosseum.utility.arcade.GameType
import org.bukkit.Location
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

internal class TestMapGameTypeCommand {
    companion object {
        private const val uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf"

        @TempDir
        @JvmField
        var tempWorldContainer: File? = null

        @TempDir
        @JvmField
        var tempPluginDataDir: File? = null
    }
    
    private var plugin: DummySite? = null
    private lateinit var player1: ConstructionSitePlayerMock
    private lateinit var world: ConstructionSiteWorldMock
    private lateinit var worldLobby: ConstructionSiteWorldMock
    private lateinit var worldMap: ConstructionSiteWorldMock

    @BeforeAll
    fun setup() {
        tearDown()

        plugin = DummySite3(
            tempWorldContainer,
            tempPluginDataDir
        )

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

        plugin!!.load()
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(worldMap)
        Assertions.assertEquals(worldMap, MockBukkit.getMock().getWorld(WorldUtils.getWorldRelativePath(worldMap)))
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs())
        Utils.writeMapData(
            WorldUtils.getWorldFolder(worldMap), String.format(
                """
                currentlyLive:true
                warps:
                MAP_NAME:MAPINFO 1234999GAMETYPE
                MAP_AUTHOR:MAPAUTHOR
                GAME_TYPE:DragonEscape
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
        val command = MapGameTypeCommand()
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        Assertions.assertTrue(manager.teleportToServerSpawn(player1))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(command.canRun(player1))
        player1.assertSaid("§cYou are in \"world\"!")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(command.canRun(player1))
        player1.assertSaid("§cYou are in \"world_lobby\"!")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertTrue(command.canRun(player1))
        player1.assertNoMoreSaid()
    }

    @Order(2)
    @Test
    fun test() {
        val command = MapGameTypeCommand()
        val teleportManager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        val mapDataManager: MapDataManager =
            ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)
        val label: String = command.aliases[0]

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf()))
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("", "")))
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("None")))
        var message: String = player1.nextMessage()
        Assertions.assertTrue(message.startsWith("§cValid game types:"))
        Assertions.assertFalse(message.contains("None"))
        player1.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("abc123")))
        message = player1.nextMessage()
        Assertions.assertTrue(message.startsWith("§cValid game types:"))
        player1.assertNoMoreSaid()

        Assertions.assertEquals(GameType.DragonEscape, mapDataManager.get(worldMap).mapGameType)
        Assertions.assertTrue(
            command.runConstruction(
                player1,
                label,
                arrayOf(GameType.NanoGames.name)
            )
        )
        player1.assertSaid("Map MAPINFO 1234999GAMETYPE: Set GameType to " + GameType.NanoGames.name)
        player1.assertNoMoreSaid()
        Assertions.assertEquals(GameType.NanoGames, mapDataManager.get(worldMap).mapGameType)
        Assertions.assertTrue((mapDataManager.get(worldMap) as MutableMapData).write())
        val data: MapData = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap))
        Assertions.assertEquals(GameType.NanoGames, data.mapGameType)
    }
}
