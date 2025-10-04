package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.AbstractTeleportCommand;
import colosseum.construction.command.TeleportHubCommand;
import colosseum.construction.command.TeleportMapCommand;
import colosseum.construction.command.TeleportSpawnCommand;
import colosseum.construction.command.TeleportWarpCommand;
import colosseum.construction.command.vanilla.TeleportCommand;
import colosseum.construction.manager.TeleportManager;
import colosseum.construction.test.dummies.ConstructionSitePlayerMock;
import colosseum.construction.test.dummies.ConstructionSiteServerMock;
import colosseum.construction.test.dummies.ConstructionSiteWorldMock;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite3;
import colosseum.construction.test.dummies.data.DummyMapDataRead;
import colosseum.utility.WorldMapConstants;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

public class TestTeleportCommands {
    private static DummySite plugin;
    private static final String uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf";
    private static ConstructionSitePlayerMock player1;
    private static final String uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693";
    private static ConstructionSitePlayerMock player2;
    private static final String uuid3 = "3e65ea50-cd1a-45fb-81d7-7e27c14662d4";
    private static ConstructionSitePlayerMock player3;
    private static ConstructionSiteWorldMock world;
    private static ConstructionSiteWorldMock worldLobby;
    private static ConstructionSiteWorldMock worldMap;
    private static DummyMapDataRead mapData;

    @TempDir
    static File tempWorldContainer;
    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    static void setup() {
        plugin = new DummySite3(tempWorldContainer, tempPluginDataDir);

        world = new ConstructionSiteWorldMock(WorldMapConstants.WORLD);
        worldLobby = new ConstructionSiteWorldMock(WorldMapConstants.WORLD_LOBBY);
        worldMap = new ConstructionSiteWorldMock("test_map", true);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(world);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldLobby);
        Assertions.assertEquals(worldLobby, MockBukkit.getMock().getWorld(WorldMapConstants.WORLD_LOBBY));
        Assertions.assertEquals(world, MockBukkit.getMock().getWorld(WorldMapConstants.WORLD));

        player1 = new ConstructionSitePlayerMock("test1", UUID.fromString(uuid1));
        player1.setOp(false);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player1);
        player2 = new ConstructionSitePlayerMock("test2", UUID.fromString(uuid2));
        player2.setOp(true);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player2);
        player3 = new ConstructionSitePlayerMock("test3", UUID.fromString(uuid3));
        player3.setOp(false);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player3);

        plugin.load();
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldMap);
        // I don't fucking know why MockBukkit.getMock().getWorld("test_map") and/or MockBukkit.getMock().getWorld("map/test_map") can break the whole test suite.
        worldMap.setSpawnLocation(8, 9, -10);
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs());
        mapData = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap), String.format("""
                currentlyLive:true
                warps:k1@0,0,0;k2@0,1,0;
                MAP_NAME:Test map
                MAP_AUTHOR:Test author
                GAME_TYPE:None
                ADMIN_LIST:%s
                """.trim(), uuid1));
        plugin.enable();
    }

    @AfterAll
    static void tearDown() {
        plugin.disable();
        MockBukkit.unload();
    }

    @Test
    void testPermission() {
        AbstractTeleportCommand[] commands = new AbstractTeleportCommand[] {
                new TeleportCommand(),
                new TeleportSpawnCommand(),
                new TeleportHubCommand(),
                new TeleportMapCommand()
        };
        for (AbstractTeleportCommand command : commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertTrue(command.canRun(player1));
            Assertions.assertTrue(command.canRun(player2));
            Assertions.assertTrue(command.canRun(player3));
        }

        AbstractTeleportCommand warpCommand = new TeleportWarpCommand();
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);

        manager.teleportToServerSpawn(player1);
        manager.teleportToServerSpawn(player2);
        manager.teleportToServerSpawn(player3);
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(warpCommand.canRun(player1));
        Assertions.assertFalse(warpCommand.canRun(player2));
        Assertions.assertFalse(warpCommand.canRun(player3));
        player1.assertSaid("§cCannot use warps in lobby!");
        player1.assertNoMoreSaid();
        player1.assertGameMode(GameMode.ADVENTURE);
        player2.assertSaid("§cCannot use warps in lobby!");
        player2.assertNoMoreSaid();
        player2.assertGameMode(GameMode.ADVENTURE);
        player3.assertSaid("§cCannot use warps in lobby!");
        player3.assertNoMoreSaid();
        player3.assertGameMode(GameMode.ADVENTURE);
        Assertions.assertFalse(player1.isFlying());
        Assertions.assertFalse(player2.isFlying());
        Assertions.assertFalse(player3.isFlying());

        manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0));
        manager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0));
        manager.teleportPlayer(player3, new Location(worldLobby, 0, 0, 0));
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(warpCommand.canRun(player1));
        Assertions.assertFalse(warpCommand.canRun(player2));
        Assertions.assertFalse(warpCommand.canRun(player3));
        player1.assertSaid("§cCannot use warps in lobby!");
        player1.assertNoMoreSaid();
        player2.assertSaid("§cCannot use warps in lobby!");
        player2.assertNoMoreSaid();
        player3.assertSaid("§cCannot use warps in lobby!");
        player3.assertNoMoreSaid();

        Assertions.assertTrue(manager.canTeleportTo(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.canTeleportTo(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(manager.canTeleportTo(player3, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(manager.teleportPlayer(player3, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(warpCommand.canRun(player1));
        Assertions.assertTrue(warpCommand.canRun(player2));
        Assertions.assertFalse(warpCommand.canRun(player3));
        player3.assertSaid("§cCannot use warps in lobby!");
        player3.assertNoMoreSaid();
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();
    }

    @Test
    void testHubCommand() {
        AbstractTeleportCommand command = new TeleportHubCommand();
        String label = command.getAliases().get(0);
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        player1.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        player2.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();

        command.runConstruction(player1, label, new String[]{});
        command.runConstruction(player2, label, new String[]{});
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();
        player1.assertLocation(new Location(world, 0, 106, 0), 1);
        player2.assertLocation(new Location(world, 0, 106, 0), 1);
    }

    @Test
    void testSpawnCommand() {
        AbstractTeleportCommand command = new TeleportSpawnCommand();
        String label = command.getAliases().get(0);
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertTrue(manager.teleportToServerSpawn(player2));
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(world, 1, 2, 3)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(world, 1, 2, 3)));

        command.runConstruction(player1, label, new String[]{});
        player1.assertSaid("Teleported to 0,106,0");
        player1.assertNoMoreSaid();
        command.runConstruction(player2, label, new String[]{});
        player2.assertSaid("Teleported to 0,106,0");
        player2.assertNoMoreSaid();
        player1.assertLocation(new Location(world, 0, 106, 0), 1);
        player2.assertLocation(new Location(world, 0, 106, 0), 1);

        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 1, 2, 3)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 1, 2, 3)));
        command.runConstruction(player1, label, new String[]{});
        player1.assertSaid("Teleported to 8,9,-10");
        player1.assertNoMoreSaid();
        command.runConstruction(player2, label, new String[]{});
        player2.assertSaid("Teleported to 8,9,-10");
        player2.assertNoMoreSaid();
        player1.assertLocation(new Location(worldMap, 8, 9, -10), 1);
        player2.assertLocation(new Location(worldMap, 8, 9, -10), 1);
    }

    @Test
    void testTeleportMapCommand() {
        AbstractTeleportCommand command = new TeleportMapCommand();
        String label = command.getAliases().get(0);
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertTrue(manager.teleportToServerSpawn(player2));
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(world, 1, 2, 3)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(world, 1, 2, 3)));

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{worldMap.getUID().toString()}));
        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{worldMap.getUID().toString()}));
        player1.assertNoMoreSaid();
        player1.assertGameMode(GameMode.CREATIVE);
        Assertions.assertTrue(player1.isFlying());
        player2.assertNoMoreSaid();
        player2.assertGameMode(GameMode.CREATIVE);
        Assertions.assertTrue(player2.isFlying());
        player1.assertLocation(new Location(worldMap, 8, 9, -10), 1);
        player2.assertLocation(new Location(worldMap, 8, 9, -10), 1);
    }
}
