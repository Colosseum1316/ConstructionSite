package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.MapCurrentlyLiveCommand;
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

class TestMapCurrentlyLiveCommand {
    private DummySite plugin;
    private static final String uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf";
    private ConstructionSitePlayerMock player1;
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

        plugin.load();
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldMap);
        Assertions.assertEquals(worldMap, MockBukkit.getMock().getWorld(WorldUtils.getWorldRelativePath(worldMap)));
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs());
        Utils.writeMapData(WorldUtils.getWorldFolder(worldMap), String.format("""
                currentlyLive:true
                warps:
                MAP_NAME:MAPINFO 123456LIVE
                MAP_AUTHOR:MAPAUTHOR 10111213
                GAME_TYPE:DragonEscape
                ADMIN_LIST:%s
                """.trim(), uuid1));
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Order(1)
    @Test
    void testPermission() {
        MapCurrentlyLiveCommand command = new MapCurrentlyLiveCommand();
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        player1.assertSaid("§cYou are in \"world\"!");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        player1.assertSaid("§cYou are in \"world_lobby\"!");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(command.canRun(player1));
        player1.assertNoMoreSaid();
    }

    @Order(2)
    @Test
    void test() {
        MapCurrentlyLiveCommand command = new MapCurrentlyLiveCommand();
        TeleportManager teleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        MapDataManager mapDataManager = ConstructionSiteProvider.getSite().getManager(MapDataManager.class);
        String labelIsLive = command.getAliases().get(0);
        String labelSetLive = command.getAliases().get(1);

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(command.runConstruction(player1, labelIsLive, new String[]{}));
        player1.assertSaid("§eMAPINFO 123456LIVE§r is §alive");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(mapDataManager.get(worldMap).isLive());
        MapData data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertTrue(data.isLive());

        Assertions.assertTrue(command.runConstruction(player1, labelSetLive, new String[]{}));
        player1.assertSaid("MAPINFO 123456LIVE is no longer live");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(mapDataManager.get(worldMap).isLive());
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertFalse(data.isLive());

        Assertions.assertTrue(command.runConstruction(player1, labelIsLive, new String[]{}));
        player1.assertSaid("§eMAPINFO 123456LIVE§r is §cnot live");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(mapDataManager.get(worldMap).isLive());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertFalse(data.isLive());

        Assertions.assertTrue(command.runConstruction(player1, labelSetLive, new String[]{}));
        player1.assertSaid("MAPINFO 123456LIVE is now live");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(mapDataManager.get(worldMap).isLive());
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertTrue(data.isLive());

        Assertions.assertTrue(command.runConstruction(player1, labelIsLive, new String[]{}));
        player1.assertSaid("§eMAPINFO 123456LIVE§r is §alive");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(mapDataManager.get(worldMap).isLive());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertTrue(data.isLive());
    }
}
