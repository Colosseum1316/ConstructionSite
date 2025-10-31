package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.command.MapInfoCommand
import colosseum.construction.manager.TeleportManager
import colosseum.construction.test.dummies.ConstructionSitePlayerMock
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.WorldMapConstants
import org.bukkit.Location
import org.bukkit.entity.Player
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

internal class TestMapInfoCommand {
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

        plugin!!.load()
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(worldMap)
        Assertions.assertEquals(worldMap, MockBukkit.getMock().getWorld(WorldUtils.getWorldRelativePath(worldMap)))
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs())
        Utils.writeMapData(
            WorldUtils.getWorldFolder(worldMap), String.format(
                """
                currentlyLive:true
                warps:
                MAP_NAME:MAPINFO 123456789MAPINFO
                MAP_AUTHOR:MAPAUTHOR 101112131415MAPINFO
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
        val command = MapInfoCommand()
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
        val command = MapInfoCommand()
        val label = command.aliases[0]
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf()))
        player1.assertSaid("Map name: §eMAPINFO 123456789MAPINFO")
        player1.assertSaid("Author: §eMAPAUTHOR 101112131415MAPINFO")
        player1.assertSaid("GameType: §eDragonEscape§r (§eDragon Escape§r)")
        player1.assertNoMoreSaid()
    }
}
