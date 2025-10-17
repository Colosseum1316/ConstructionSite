package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.AbstractMapCreditCommand;
import colosseum.construction.command.MapAuthorCommand;
import colosseum.construction.command.MapNameCommand;
import colosseum.construction.data.MutableMapData;
import colosseum.construction.manager.MapDataManager;
import colosseum.construction.manager.TeleportManager;
import colosseum.construction.test.dummies.ConstructionSitePlayerMock;
import colosseum.construction.test.dummies.ConstructionSiteServerMock;
import colosseum.construction.test.dummies.ConstructionSiteWorldMock;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite3;
import colosseum.utility.MapData;
import colosseum.utility.WorldMapConstants;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

class TestMapCreditCommands {
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
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs());
        Utils.writeMapData(WorldUtils.getWorldFolder(worldMap), String.format("""
                currentlyLive:true
                warps:k1@-1,2,-3;k2@-5,6,-7;
                MAP_NAME:map none
                MAP_AUTHOR:author none
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
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertTrue(manager.teleportToServerSpawn(player2));
        Assertions.assertTrue(manager.teleportToServerSpawn(player3));

        AbstractMapCreditCommand[] commands = new AbstractMapCreditCommand[]{
                new MapAuthorCommand(),
                new MapNameCommand(),
        };

        for (AbstractMapCreditCommand command : commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertFalse(command.canRun(player1));
            Assertions.assertFalse(command.canRun(player2));
            Assertions.assertFalse(command.canRun(player3));
            player1.assertSaid("§cYou are in \"world\"!");
            player1.assertNoMoreSaid();
            player2.assertSaid("§cYou are in \"world\"!");
            player2.assertNoMoreSaid();
            player3.assertSaid("§cYou are in \"world\"!");
            player3.assertNoMoreSaid();
        }

        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player3, new Location(worldLobby, 0, 0, 0)));

        for (AbstractMapCreditCommand command : commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertFalse(command.canRun(player1));
            Assertions.assertFalse(command.canRun(player2));
            Assertions.assertFalse(command.canRun(player3));
            player1.assertSaid("§cYou are in \"world_lobby\"!");
            player1.assertNoMoreSaid();
            player2.assertSaid("§cYou are in \"world_lobby\"!");
            player2.assertNoMoreSaid();
            player3.assertSaid("§cYou are in \"world_lobby\"!");
            player3.assertNoMoreSaid();
        }

        Assertions.assertTrue(manager.canTeleportTo(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.canTeleportTo(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(manager.canTeleportTo(player3, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(manager.teleportPlayer(player3, new Location(worldMap, 0, 0, 0)));

        for (AbstractMapCreditCommand command : commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertTrue(command.canRun(player1));
            Assertions.assertTrue(command.canRun(player2));
            Assertions.assertFalse(command.canRun(player3));
            player3.assertSaid("§cYou are in \"world_lobby\"!");
            player3.assertNoMoreSaid();
            player1.assertNoMoreSaid();
            player2.assertNoMoreSaid();
        }
    }

    @Order(2)
    @Test
    void testMapAuthorCommand() {
        AbstractMapCreditCommand command = new MapAuthorCommand();
        String label = command.getAliases().get(0);
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        MapDataManager mapDataManager = ConstructionSiteProvider.getSite().getManager(MapDataManager.class);
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        player1.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        player2.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player2, label, new String[]{}));

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"It's", "player", "1"}));
        player1.assertSaid("Set author: It's player 1");
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();
        Assertions.assertEquals("It's player 1", mapDataManager.get(worldMap).getMapCreator());
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        MapData data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertEquals("It's player 1", data.getMapCreator());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"This", "is", "player", "2"}));
        player2.assertSaid("Set author: This is player 2");
        player2.assertNoMoreSaid();
        player1.assertNoMoreSaid();
        Assertions.assertEquals("This is player 2", mapDataManager.get(worldMap).getMapCreator());
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertEquals("This is player 2", data.getMapCreator());
    }

    @Order(3)
    @Test
    void testMapNameCommand() {
        AbstractMapCreditCommand command = new MapNameCommand();
        String label = command.getAliases().get(0);
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        MapDataManager mapDataManager = ConstructionSiteProvider.getSite().getManager(MapDataManager.class);
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        player1.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        player2.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player2, label, new String[]{}));

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"Map", "v1"}));
        player1.assertSaid("Set map name: Map v1");
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();
        Assertions.assertEquals("Map v1", mapDataManager.get(worldMap).getMapName());
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        MapData data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertEquals("Map v1", data.getMapName());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"Map", "v2"}));
        player2.assertSaid("Set map name: Map v2");
        player2.assertNoMoreSaid();
        player1.assertNoMoreSaid();
        Assertions.assertEquals("Map v2", mapDataManager.get(worldMap).getMapName());
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertEquals("Map v2", data.getMapName());
    }
}
