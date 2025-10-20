package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.MapDeleteCommand;
import colosseum.construction.manager.MapDataManager;
import colosseum.construction.manager.TeleportManager;
import colosseum.construction.test.dummies.ConstructionSitePlayerMock;
import colosseum.construction.test.dummies.ConstructionSiteServerMock;
import colosseum.construction.test.dummies.ConstructionSiteWorldMock;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite3;
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

class TestMapDeleteCommand {
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
                MAP_NAME:Test mapdelete
                MAP_AUTHOR:Test mapdelete
                GAME_TYPE:None
                ADMIN_LIST:%s,%s
                """.trim(), uuid1, uuid2));
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Order(1)
    @Test
    void testPermission() {
        MapDeleteCommand command = new MapDeleteCommand();
        TeleportManager teleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);

        Assertions.assertTrue(teleportManager.teleportToServerSpawn(player1));
        Assertions.assertTrue(teleportManager.teleportToServerSpawn(player2));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        player1.assertSaid("§cYou are in \"world\"!");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.canRun(player2));
        player2.assertSaid("§cYou are in \"world\"!");
        player2.assertNoMoreSaid();

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertTrue(teleportManager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        player1.assertSaid("§cYou are in \"world_lobby\"!");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.canRun(player2));
        player2.assertSaid("§cYou are in \"world_lobby\"!");
        player2.assertNoMoreSaid();

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(teleportManager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(command.canRun(player1));
        Assertions.assertTrue(command.canRun(player2));
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();
    }

    @Order(2)
    @Test
    void test() {
        MapDeleteCommand command = new MapDeleteCommand();
        String label = command.getAliases().get(0);
        TeleportManager teleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        MapDataManager mapDataManager =  ConstructionSiteProvider.getSite().getManager(MapDataManager.class);
        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(teleportManager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        UUID uid = worldMap.getUID();
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{}));
        player1.assertSaid(String.format("§e%s", uid));
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"", ""}));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{UUID.randomUUID().toString()}));
        player1.assertSaid("§cUUID mismatch!");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"123"}));
        player1.assertSaid("§cInvalid input!");
        player1.assertNoMoreSaid();

        Assertions.assertTrue(WorldUtils.getWorldFolder(worldMap).exists());
        Assertions.assertTrue(MockBukkit.getMock().getWorlds().contains(worldMap));
        String relative = WorldUtils.getWorldRelativePath(worldMap);
        player1.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        player2.assertLocation(new Location(worldMap, 0, 0, 0), 1);
        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{uid.toString()}));
        player2.assertSaid("Deleting world " + relative);
        player2.assertNoMoreSaid();
        Assertions.assertFalse(WorldUtils.getWorldFolder(worldMap).exists());
        Assertions.assertFalse(MockBukkit.getMock().getWorlds().contains(worldMap));
        player1.assertLocation(new Location(world, 0, 106, 0), 1);
        player2.assertLocation(new Location(world, 0, 106, 0), 1);
    }
}
