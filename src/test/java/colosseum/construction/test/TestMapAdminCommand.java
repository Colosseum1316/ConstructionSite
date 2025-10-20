package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.MapAdminCommand;
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

class TestMapAdminCommand {
    private DummySite plugin;
    private static final String uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf";
    private ConstructionSitePlayerMock player1;
    private static final String uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693";
    private ConstructionSitePlayerMock player2;
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
        player1.setOp(false);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player1);
        player2 = new ConstructionSitePlayerMock("test2", UUID.fromString(uuid2));
        player2.setOp(false);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player2);

        plugin.load();
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldMap);
        Assertions.assertEquals(worldMap, MockBukkit.getMock().getWorld(WorldUtils.getWorldRelativePath(worldMap)));
        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).mkdirs());
        Utils.writeMapData(WorldUtils.getWorldFolder(worldMap), String.format("""
                currentlyLive:true
                warps:
                MAP_NAME:Test mapadmin
                MAP_AUTHOR:Test mapadmin
                GAME_TYPE:None
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
        MapAdminCommand command = new MapAdminCommand();
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertTrue(manager.teleportToServerSpawn(player2));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        Assertions.assertFalse(command.canRun(player2));
        player1.assertSaid("§cYou are in \"world\"!");
        player1.assertNoMoreSaid();
        player2.assertSaid("§cYou are in \"world\"!");
        player2.assertNoMoreSaid();
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        Assertions.assertFalse(command.canRun(player2));
        player1.assertSaid("§cYou are in \"world_lobby\"!");
        player1.assertNoMoreSaid();
        player2.assertSaid("§cYou are in \"world_lobby\"!");
        player2.assertNoMoreSaid();
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(command.canRun(player1));
        Assertions.assertFalse(command.canRun(player2));
        player1.assertNoMoreSaid();
        player2.assertSaid("§cYou are in \"world_lobby\"!");
        player2.assertNoMoreSaid();
    }

    @Order(2)
    @Test
    void test() {
        MapAdminCommand command = new MapAdminCommand();
        String label = command.getAliases().get(0);
        TeleportManager teleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        MapDataManager mapDataManager = ConstructionSiteProvider.getSite().getManager(MapDataManager.class);

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"", ""}));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"t"}));
        Assertions.assertNotNull(player1.nextMessage());
        player1.assertNoMoreSaid();
        Assertions.assertTrue(mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid1)));
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player1));
        Assertions.assertFalse(mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid2)));
        Assertions.assertFalse(mapDataManager.get(worldMap).allows(player2));
        MapData data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertNotNull(data);
        Assertions.assertTrue(data.allows(player1));
        Assertions.assertFalse(data.allows(player2));
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid1)));
        Assertions.assertFalse(data.adminList().contains(UUID.fromString(uuid2)));

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"test2"}));
        player1.assertSaid("test2 is now admin in Test mapadmin");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid1)));
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player1));
        Assertions.assertTrue(mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid2)));
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player2));
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertTrue(data.allows(player1));
        Assertions.assertTrue(data.allows(player2));
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid1)));
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid2)));

        Assertions.assertTrue(teleportManager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"test1"}));
        player2.assertSaid("test1 is no longer admin in Test mapadmin");
        player2.assertNoMoreSaid();
        Assertions.assertFalse(mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid1)));
        Assertions.assertFalse(mapDataManager.get(worldMap).allows(player1));
        Assertions.assertTrue(mapDataManager.get(worldMap).adminList().contains(UUID.fromString(uuid2)));
        Assertions.assertTrue(mapDataManager.get(worldMap).allows(player2));
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertFalse(data.allows(player1));
        Assertions.assertTrue(data.allows(player2));
        Assertions.assertFalse(data.adminList().contains(UUID.fromString(uuid1)));
        Assertions.assertTrue(data.adminList().contains(UUID.fromString(uuid2)));
    }
}
