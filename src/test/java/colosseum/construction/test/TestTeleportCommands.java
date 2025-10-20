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
import colosseum.construction.manager.MapDataManager;
import colosseum.construction.manager.TeleportManager;
import colosseum.construction.test.dummies.ConstructionSitePlayerMock;
import colosseum.construction.test.dummies.ConstructionSiteServerMock;
import colosseum.construction.test.dummies.ConstructionSiteWorldMock;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite3;
import colosseum.utility.WorldMapConstants;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

class TestTeleportCommands {
    private DummySite plugin;
    private static final String uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf";
    private ConstructionSitePlayerMock player1;
    private static final String uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693";
    private ConstructionSitePlayerMock player2;
    private static final String uuid3 = "3e65ea50-cd1a-45fb-81d7-7e27c14662d4";
    private ConstructionSitePlayerMock player3;
    private ConstructionSiteWorldMock world;
    private ConstructionSiteWorldMock worldLobby;
    private ConstructionSiteWorldMock worldMap;

    @TempDir
    static File tempWorldContainer;
    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    void setup() {
        tearDown();

        plugin = new DummySite3(tempWorldContainer, tempPluginDataDir);

        world = new ConstructionSiteWorldMock(WorldMapConstants.WORLD);
        worldLobby = new ConstructionSiteWorldMock(WorldMapConstants.WORLD_LOBBY);
        worldMap = new ConstructionSiteWorldMock("test_map", true);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(world);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldLobby);
        Assertions.assertEquals(worldLobby, MockBukkit.getMock().getWorld(WorldMapConstants.WORLD_LOBBY));
        Assertions.assertEquals(world, MockBukkit.getMock().getWorld(WorldMapConstants.WORLD));

        player1 = new ConstructionSitePlayerMock("test1", UUID.fromString(uuid1));
        player1.setOp(true);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player1);
        player2 = new ConstructionSitePlayerMock("test2", UUID.fromString(uuid2));
        player2.setOp(false);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player2);
        player3 = new ConstructionSitePlayerMock("test3", UUID.fromString(uuid3));
        player3.setOp(false);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player3);

        plugin.load();
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldMap);
        Assertions.assertEquals(worldMap, MockBukkit.getMock().getWorld(WorldUtils.getWorldRelativePath(worldMap)));
        worldMap.setSpawnLocation(8, 9, -10);
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs());
        Utils.writeMapData(WorldUtils.getWorldFolder(worldMap), String.format("""
                currentlyLive:true
                warps:k1@-1,2,-3;k2@-5,6,-7;
                MAP_NAME:Test map1mapteleport
                MAP_AUTHOR:Test author2mapteleport
                GAME_TYPE:None
                ADMIN_LIST:%s
                """.trim(), uuid2));
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Order(1)
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

        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertTrue(manager.teleportToServerSpawn(player2));
        Assertions.assertTrue(manager.teleportToServerSpawn(player3));
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

        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player3, new Location(worldLobby, 0, 0, 0)));
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

    @Order(2)
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

    @Order(3)
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

    @Order(4)
    @Test
    void testTeleportMapCommand() {
        AbstractTeleportCommand command = new TeleportMapCommand();
        String label = command.getAliases().get(0);
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertTrue(manager.teleportToServerSpawn(player2));
        Assertions.assertTrue(manager.teleportToServerSpawn(player3));
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(world, 1, 2, 3)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(world, 1, 2, 3)));
        Assertions.assertTrue(manager.teleportPlayer(player3, new Location(world, 1, 2, 3)));

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"invalid"}));
        player1.assertSaid("§cInvalid UUID!");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"invalid", "invalid"}));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{}));
        player1.assertSaid("§7Test map1mapteleport - Test author2mapteleport (None): §e" + worldMap.getUID());
        player1.assertNoMoreSaid();

        // Possibility of UUID.randomUUID().equals(worldMap.getUID()) is practically zero.
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{UUID.randomUUID().toString()}));
        player1.assertSaid("§cUnknown world!");
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{worldMap.getUID().toString()}));
        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{worldMap.getUID().toString()}));
        Assertions.assertTrue(command.runConstruction(player3, label, new String[]{worldMap.getUID().toString()}));
        player3.assertSaid("§cTeleportation unsuccessful...");
        player3.assertNoMoreSaid();
        player3.assertLocation(new Location(world, 1, 2, 3), 1);
        player1.assertNoMoreSaid();
        player1.assertGameMode(GameMode.CREATIVE);
        Assertions.assertTrue(player1.isFlying());
        player2.assertNoMoreSaid();
        player2.assertGameMode(GameMode.CREATIVE);
        Assertions.assertTrue(player2.isFlying());
        player1.assertLocation(new Location(worldMap, 8, 9, -10), 1);
        player2.assertLocation(new Location(worldMap, 8, 9, -10), 1);
    }

    @Order(5)
    @Test
    void testWarpCommand() {
        AbstractTeleportCommand command = new TeleportWarpCommand();
        String label = command.getAliases().get(0);
        TeleportManager teleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        MapDataManager mapDataManager = ConstructionSiteProvider.getSite().getManager(MapDataManager.class);

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 1, 2, 3)));

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"invalid", "invalid", "invalid"}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"set"}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"delete"}));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"list"}));
        player1.assertSaid("§ek1: -1.0,2.0,-3.0");
        player1.assertSaid("§ek2: -5.0,6.0,-7.0");
        player1.assertNoMoreSaid();

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"0"}));
        player1.assertSaid("§cInvalid input.");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"a"}));
        player1.assertSaid("§cInvalid input.");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{" "}));
        player1.assertSaid("§cInvalid input.");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"ab"}));
        player1.assertSaid("§cUnknown warp point \"ab\"");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"k1"}));
        player1.assertSaid("Teleported to warp point §ek1");
        player1.assertNoMoreSaid();
        player1.assertLocation(new Location(worldMap, -1, 2, -3), 1);
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"k2"}));
        player1.assertSaid("Teleported to warp point §ek2");
        player1.assertNoMoreSaid();
        player1.assertLocation(new Location(worldMap, -5, 6, -7), 1);

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"delete", "k3"}));
        player1.assertSaid("§c\"k3\" does not exist!");
        player1.assertNoMoreSaid();
        Assertions.assertEquals(2, mapDataManager.get(worldMap).warps().size());
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"delete", "k1"}));
        player1.assertSaid("Deleting warp point §ek1");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"delete", "k1"}));
        player1.assertSaid("§c\"k1\" does not exist!");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"delete", "k2"}));
        player1.assertSaid("Deleting warp point §ek2");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"delete", "k2"}));
        player1.assertSaid("§c\"k2\" does not exist!");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"list"}));
        player1.assertSaid("§cNo warp point yet! Add some!");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(mapDataManager.get(worldMap).warps().isEmpty());

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"list", "aa"}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"set", "s"}));
        player1.assertSaid("§cInvalid input.");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"delete", "1s"}));
        player1.assertSaid("§cInvalid input.");
        player1.assertNoMoreSaid();
        for (String a : new String[]{"set", "delete"}) {
            for (String b : new String[]{"list", "set", "delete"}) {
                Assertions.assertFalse(command.runConstruction(player1, label, new String[]{a, b}));
                player1.assertSaid("§cYou can't use \"list\", \"delete\" or \"set\" as warp point name!");
                player1.assertNoMoreSaid();
            }
        }

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 10, 11, 12)));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"set", "n1"}));
        player1.assertSaid("Created warp point §en1");
        player1.assertNoMoreSaid();
        Assertions.assertEquals(1, mapDataManager.get(worldMap).warps().size());
        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 15, 17, 19)));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"set", "n1"}));
        player1.assertSaid("§c\"n1\" already exists!");
        player1.assertNoMoreSaid();
        Assertions.assertEquals(1, mapDataManager.get(worldMap).warps().size());
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"list"}));
        player1.assertSaid("§en1: 10.0,11.0,12.0");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"n1"}));
        player1.assertSaid("Teleported to warp point §en1");
        player1.assertNoMoreSaid();
        player1.assertLocation(new Location(worldMap, 10, 11, 12), 1);
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"delete", "n1"}));
        player1.assertSaid("Deleting warp point §en1");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"delete", "n1"}));
        player1.assertSaid("§c\"n1\" does not exist!");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(mapDataManager.get(worldMap).warps().isEmpty());
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"list"}));
        player1.assertSaid("§cNo warp point yet! Add some!");
        player1.assertNoMoreSaid();
    }

    @Order(6)
    @Test
    void testTeleportCommand() {
        AbstractTeleportCommand command = new TeleportCommand();
        String label = command.getAliases().get(0);
        TeleportManager teleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(world, 5, 6, 7)));
        Assertions.assertTrue(teleportManager.teleportPlayer(player2, new Location(worldMap, 8, 9, 10)));
        Assertions.assertTrue(teleportManager.teleportPlayer(player3, new Location(worldLobby, 1, 2, 3)));

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"1", "2", "3", "4"}));

        player1.assertLocation(new Location(world, 5, 6, 7), 1);
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"~-2", "9", "~2"}));
        player1.assertLocation(new Location(world, 3, 9, 9), 1);
        player1.assertSaid("You teleported to §e3,9,9");
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"test1"}));
        player1.assertLocation(new Location(world, 3, 9, 9), 1);
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"test2"}));
        player1.assertLocation(new Location(worldMap, 8, 9, 10), 1);
        player1.assertSaid("You teleported to §etest2");
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"test1", "test3"}));
        player1.assertLocation(new Location(worldLobby, 1, 2, 3), 1);
        player1.assertSaid("You teleported to §etest3");
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"test3", "test2"}));
        player3.assertLocation(new Location(worldLobby, 1, 2, 3), 1);
        player3.assertNoMoreSaid();
        player1.assertSaid("§cTeleportation unsuccessful...");
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player3, label, new String[]{"test2"}));
        player3.assertLocation(new Location(worldLobby, 1, 2, 3), 1);
        player3.assertSaid("§cTeleportation unsuccessful...");
        player3.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"test1", "test3"}));
        player1.assertLocation(new Location(worldLobby, 1, 2, 3), 1);
        player2.assertSaid("§cTeleportation unsuccessful...");
        player2.assertNoMoreSaid();
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"test2", "test3"}));
        player2.assertLocation(new Location(worldLobby, 1, 2, 3), 1);
        player1.assertSaid("You teleported §etest2§r to §etest3");
        player1.assertNoMoreSaid();
        player2.assertSaid("§etest1§r teleported you to §etest3");
        player2.assertNoMoreSaid();

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(world, 5, 6, 7)));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"test3", "test1"}));
        player3.assertLocation(new Location(world, 5, 6, 7), 1);
        player3.assertSaid("You are teleported to §etest1");
        player3.assertNoMoreSaid();
        player1.assertSaid("You teleported §etest3§r to you");
        player1.assertNoMoreSaid();
    }
}
