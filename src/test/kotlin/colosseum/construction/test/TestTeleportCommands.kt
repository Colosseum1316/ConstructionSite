package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.command.AbstractTeleportCommand
import colosseum.construction.command.TeleportHubCommand
import colosseum.construction.command.TeleportMapCommand
import colosseum.construction.command.TeleportSpawnCommand
import colosseum.construction.command.TeleportWarpCommand
import colosseum.construction.command.vanilla.TeleportCommand
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.TeleportManager
import colosseum.construction.test.dummies.ConstructionSitePlayerMock
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.WorldMapConstants
import org.bukkit.GameMode
import org.bukkit.Location
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

internal class TestTeleportCommands {
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
        worldMap.setSpawnLocation(8, 9, -10)
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs())
        Utils.writeMapData(
            WorldUtils.getWorldFolder(worldMap), String.format(
                """
                currentlyLive:true
                warps:k1@-1,2,-3;k2@-5,6,-7;
                MAP_NAME:Test map1mapteleport
                MAP_AUTHOR:Test author2mapteleport
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
        val commands: Array<AbstractTeleportCommand> = arrayOf(
            TeleportCommand(),
            TeleportSpawnCommand(),
            TeleportHubCommand(),
            TeleportMapCommand()
        )
        for (command in commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().consoleSender))
            Assertions.assertTrue(command.canRun(player1))
            Assertions.assertTrue(command.canRun(player2))
            Assertions.assertTrue(command.canRun(player3))
        }

        val warpCommand: AbstractTeleportCommand = TeleportWarpCommand()
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager<TeleportManager>(TeleportManager::class.java)

        Assertions.assertTrue(manager.teleportToServerSpawn(player1))
        Assertions.assertTrue(manager.teleportToServerSpawn(player2))
        Assertions.assertTrue(manager.teleportToServerSpawn(player3))
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(warpCommand.canRun(player1))
        Assertions.assertFalse(warpCommand.canRun(player2))
        Assertions.assertFalse(warpCommand.canRun(player3))
        player1.assertSaid("§cCannot use warps in lobby!")
        player1.assertNoMoreSaid()
        player1.assertGameMode(GameMode.ADVENTURE)
        player2.assertSaid("§cCannot use warps in lobby!")
        player2.assertNoMoreSaid()
        player2.assertGameMode(GameMode.ADVENTURE)
        player3.assertSaid("§cCannot use warps in lobby!")
        player3.assertNoMoreSaid()
        player3.assertGameMode(GameMode.ADVENTURE)
        Assertions.assertFalse(player1.isFlying)
        Assertions.assertFalse(player2.isFlying)
        Assertions.assertFalse(player3.isFlying)

        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player3, Location(worldLobby, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertFalse(warpCommand.canRun(player1))
        Assertions.assertFalse(warpCommand.canRun(player2))
        Assertions.assertFalse(warpCommand.canRun(player3))
        player1.assertSaid("§cCannot use warps in lobby!")
        player1.assertNoMoreSaid()
        player2.assertSaid("§cCannot use warps in lobby!")
        player2.assertNoMoreSaid()
        player3.assertSaid("§cCannot use warps in lobby!")
        player3.assertNoMoreSaid()

        Assertions.assertTrue(manager.check(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.check(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(manager.check(player3, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(manager.teleportPlayer(player3, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().consoleSender))
        Assertions.assertTrue(warpCommand.canRun(player1))
        Assertions.assertTrue(warpCommand.canRun(player2))
        Assertions.assertFalse(warpCommand.canRun(player3))
        player3.assertSaid("§cCannot use warps in lobby!")
        player3.assertNoMoreSaid()
        player1.assertNoMoreSaid()
        player2.assertNoMoreSaid()
    }

    @Order(2)
    @Test
    fun testHubCommand() {
        val command: AbstractTeleportCommand = TeleportHubCommand()
        val label: String = command.aliases[0]
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 0.0, 0.0, 0.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldMap, 0.0, 0.0, 0.0)))
        player1.assertLocation(Location(worldMap, 0.0, 0.0, 0.0), 1.0)
        player2.assertLocation(Location(worldMap, 0.0, 0.0, 0.0), 1.0)
        player1.assertNoMoreSaid()
        player2.assertNoMoreSaid()

        command.runConstruction(player1, label, arrayOf())
        command.runConstruction(player2, label, arrayOf())
        player1.assertNoMoreSaid()
        player2.assertNoMoreSaid()
        player1.assertLocation(Location(world, 0.0, 106.0, 0.0), 1.0)
        player2.assertLocation(Location(world, 0.0, 106.0, 0.0), 1.0)
    }

    @Order(3)
    @Test
    fun testSpawnCommand() {
        val command: AbstractTeleportCommand = TeleportSpawnCommand()
        val label: String = command.aliases[0]
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        Assertions.assertTrue(manager.teleportToServerSpawn(player1))
        Assertions.assertTrue(manager.teleportToServerSpawn(player2))
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(world, 1.0, 2.0, 3.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(world, 1.0, 2.0, 3.0)))

        command.runConstruction(player1, label, arrayOf())
        player1.assertSaid("Teleported to §e0,106,0")
        player1.assertNoMoreSaid()
        command.runConstruction(player2, label, arrayOf())
        player2.assertSaid("Teleported to §e0,106,0")
        player2.assertNoMoreSaid()
        player1.assertLocation(Location(world, 0.0, 106.0, 0.0), 1.0)
        player2.assertLocation(Location(world, 0.0, 106.0, 0.0), 1.0)

        Assertions.assertTrue(manager.teleportPlayer(player1, Location(worldMap, 1.0, 2.0, 3.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(worldMap, 1.0, 2.0, 3.0)))
        command.runConstruction(player1, label, arrayOf())
        player1.assertSaid("Teleported to §e8,9,-10")
        player1.assertNoMoreSaid()
        command.runConstruction(player2, label, arrayOf())
        player2.assertSaid("Teleported to §e8,9,-10")
        player2.assertNoMoreSaid()
        player1.assertLocation(Location(worldMap, 8.0, 9.0, -10.0), 1.0)
        player2.assertLocation(Location(worldMap, 8.0, 9.0, -10.0), 1.0)
    }

    @Order(4)
    @Test
    fun testTeleportMapCommand() {
        val command: AbstractTeleportCommand = TeleportMapCommand()
        val label: String = command.aliases[0]
        val manager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        Assertions.assertTrue(manager.teleportToServerSpawn(player1))
        Assertions.assertTrue(manager.teleportToServerSpawn(player2))
        Assertions.assertTrue(manager.teleportToServerSpawn(player3))
        Assertions.assertTrue(manager.teleportPlayer(player1, Location(world, 1.0, 2.0, 3.0)))
        Assertions.assertTrue(manager.teleportPlayer(player2, Location(world, 1.0, 2.0, 3.0)))
        Assertions.assertTrue(manager.teleportPlayer(player3, Location(world, 1.0, 2.0, 3.0)))

        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("invalid")))
        player1.assertSaid("§cInvalid UUID!")
        player1.assertNoMoreSaid()
        Assertions.assertFalse(
            command.runConstruction(
                player1,
                label,
                arrayOf("invalid", "invalid")
            )
        )
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf()))
        player1.assertSaid("§7Test map1mapteleport - Test author2mapteleport: §e" + worldMap.uid)
        player1.assertNoMoreSaid()

        // Possibility of UUID.randomUUID().equals(worldMap.getUID()) is practically zero.
        Assertions.assertFalse(
            command.runConstruction(
                player1,
                label,
                arrayOf(UUID.randomUUID().toString())
            )
        )
        player1.assertSaid("§cUnknown world!")
        player1.assertNoMoreSaid()

        Assertions.assertTrue(
            command.runConstruction(
                player1,
                label,
                arrayOf(worldMap.uid.toString())
            )
        )
        Assertions.assertTrue(
            command.runConstruction(
                player2,
                label,
                arrayOf(worldMap.uid.toString())
            )
        )
        Assertions.assertTrue(
            command.runConstruction(
                player3,
                label,
                arrayOf(worldMap.uid.toString())
            )
        )
        player3.assertSaid("§cTeleportation unsuccessful...")
        player3.assertNoMoreSaid()
        player3.assertLocation(Location(world, 1.0, 2.0, 3.0), 1.0)
        player1.assertNoMoreSaid()
        player1.assertGameMode(GameMode.CREATIVE)
        Assertions.assertTrue(player1.isFlying)
        player2.assertNoMoreSaid()
        player2.assertGameMode(GameMode.CREATIVE)
        Assertions.assertTrue(player2.isFlying)
        player1.assertLocation(Location(worldMap, 8.0, 9.0, -10.0), 1.0)
        player2.assertLocation(Location(worldMap, 8.0, 9.0, -10.0), 1.0)
    }

    @Order(5)
    @Test
    fun testWarpCommand() {
        val command: AbstractTeleportCommand = TeleportWarpCommand()
        val label: String = command.aliases[0]
        val teleportManager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)
        val mapDataManager: MapDataManager =
            ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java)

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(worldMap, 1.0, 2.0, 3.0)))

        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf()))
        Assertions.assertFalse(
            command.runConstruction(
                player1,
                label,
                arrayOf("invalid", "invalid", "invalid")
            )
        )
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("set")))
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("delete")))
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("list")))
        player1.assertSaid("§ek1: -1.0,2.0,-3.0")
        player1.assertSaid("§ek2: -5.0,6.0,-7.0")
        player1.assertNoMoreSaid()

        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("0")))
        player1.assertSaid("§cInvalid input.")
        player1.assertNoMoreSaid()
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("a")))
        player1.assertSaid("§cInvalid input.")
        player1.assertNoMoreSaid()
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf(" ")))
        player1.assertSaid("§cInvalid input.")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("ab")))
        player1.assertSaid("§cUnknown warp point \"ab\"")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("k1")))
        player1.assertSaid("Teleported to warp point §ek1")
        player1.assertNoMoreSaid()
        player1.assertLocation(Location(worldMap, -1.0, 2.0, -3.0), 1.0)
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("k2")))
        player1.assertSaid("Teleported to warp point §ek2")
        player1.assertNoMoreSaid()
        player1.assertLocation(Location(worldMap, -5.0, 6.0, -7.0), 1.0)

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("delete", "k3")))
        player1.assertSaid("§c\"k3\" does not exist!")
        player1.assertNoMoreSaid()
        Assertions.assertEquals(2, mapDataManager.get(worldMap).warps().size)
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("delete", "k1")))
        player1.assertSaid("Deleting warp point §ek1")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("delete", "k1")))
        player1.assertSaid("§c\"k1\" does not exist!")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("delete", "k2")))
        player1.assertSaid("Deleting warp point §ek2")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("delete", "k2")))
        player1.assertSaid("§c\"k2\" does not exist!")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("list")))
        player1.assertSaid("§cNo warp point yet! Add some!")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(mapDataManager.get(worldMap).warps().isEmpty())

        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("list", "aa")))
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("set", "s")))
        player1.assertSaid("§cInvalid input.")
        player1.assertNoMoreSaid()
        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf("delete", "1s")))
        player1.assertSaid("§cInvalid input.")
        player1.assertNoMoreSaid()
        for (a in arrayOf("set", "delete")) {
            for (b in arrayOf("list", "set", "delete")) {
                Assertions.assertFalse(command.runConstruction(player1, label, arrayOf(a, b)))
                player1.assertSaid("§cYou can't use \"list\", \"delete\" or \"set\" as warp point name!")
                player1.assertNoMoreSaid()
            }
        }

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(worldMap, 10.0, 11.0, 12.0)))
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("set", "n1")))
        player1.assertSaid("Created warp point §en1")
        player1.assertNoMoreSaid()
        Assertions.assertEquals(1, mapDataManager.get(worldMap).warps().size)
        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(worldMap, 15.0, 17.0, 19.0)))
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("set", "n1")))
        player1.assertSaid("§c\"n1\" already exists!")
        player1.assertNoMoreSaid()
        Assertions.assertEquals(1, mapDataManager.get(worldMap).warps().size)
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("list")))
        player1.assertSaid("§en1: 10.0,11.0,12.0")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("n1")))
        player1.assertSaid("Teleported to warp point §en1")
        player1.assertNoMoreSaid()
        player1.assertLocation(Location(worldMap, 10.0, 11.0, 12.0), 1.0)
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("delete", "n1")))
        player1.assertSaid("Deleting warp point §en1")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("delete", "n1")))
        player1.assertSaid("§c\"n1\" does not exist!")
        player1.assertNoMoreSaid()
        Assertions.assertTrue(mapDataManager.get(worldMap).warps().isEmpty())
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("list")))
        player1.assertSaid("§cNo warp point yet! Add some!")
        player1.assertNoMoreSaid()
    }

    @Order(6)
    @Test
    fun testTeleportCommand() {
        val command: AbstractTeleportCommand = TeleportCommand()
        val label: String = command.aliases[0]
        val teleportManager: TeleportManager =
            ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java)

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(world, 5.0, 6.0, 7.0)))
        Assertions.assertTrue(teleportManager.teleportPlayer(player2, Location(worldMap, 8.0, 9.0, 10.0)))
        Assertions.assertTrue(teleportManager.teleportPlayer(player3, Location(worldLobby, 1.0, 2.0, 3.0)))

        Assertions.assertFalse(command.runConstruction(player1, label, arrayOf()))
        Assertions.assertFalse(
            command.runConstruction(
                player1,
                label,
                arrayOf("1", "2", "3", "4")
            )
        )

        player1.assertLocation(Location(world, 5.0, 6.0, 7.0), 1.0)
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("~-2", "9", "~2")))
        player1.assertLocation(Location(world, 3.0, 9.0, 9.0), 1.0)
        player1.assertSaid("You teleported to §e3,9,9")
        player1.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("test1")))
        player1.assertLocation(Location(world, 3.0, 9.0, 9.0), 1.0)
        player1.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("test2")))
        player1.assertLocation(Location(worldMap, 8.0, 9.0, 10.0), 1.0)
        player1.assertSaid("You teleported to §etest2")
        player1.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("test1", "test3")))
        player1.assertLocation(Location(worldLobby, 1.0, 2.0, 3.0), 1.0)
        player1.assertSaid("You teleported to §etest3")
        player1.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("test3", "test2")))
        player3.assertLocation(Location(worldLobby, 1.0, 2.0, 3.0), 1.0)
        player3.assertNoMoreSaid()
        player1.assertSaid("§cTeleportation unsuccessful...")
        player1.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player3, label, arrayOf("test2")))
        player3.assertLocation(Location(worldLobby, 1.0, 2.0, 3.0), 1.0)
        player3.assertSaid("§cTeleportation unsuccessful...")
        player3.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player2, label, arrayOf("test1", "test3")))
        player1.assertLocation(Location(worldLobby, 1.0, 2.0, 3.0), 1.0)
        player2.assertSaid("§cTeleportation unsuccessful...")
        player2.assertNoMoreSaid()
        player1.assertNoMoreSaid()

        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("test2", "test3")))
        player2.assertLocation(Location(worldLobby, 1.0, 2.0, 3.0), 1.0)
        player1.assertSaid("You teleported §etest2§r to §etest3")
        player1.assertNoMoreSaid()
        player2.assertSaid("§etest1§r teleported you to §etest3")
        player2.assertNoMoreSaid()

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, Location(world, 5.0, 6.0, 7.0)))
        Assertions.assertTrue(command.runConstruction(player1, label, arrayOf("test3", "test1")))
        player3.assertLocation(Location(world, 5.0, 6.0, 7.0), 1.0)
        player3.assertSaid("You are teleported to §etest1")
        player3.assertNoMoreSaid()
        player1.assertSaid("You teleported §etest3§r to you")
        player1.assertNoMoreSaid()
    }
}
