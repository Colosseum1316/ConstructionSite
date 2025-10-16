package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.vanilla.TimeCommand;
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

class TestTimeCommand {
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
                warps:
                MAP_NAME:Test map9
                MAP_AUTHOR:Test author10
                GAME_TYPE:None
                ADMIN_LIST:%s
                """.trim(), uuid2));
        plugin.enable();

        world.setTime(0);
        worldLobby.setTime(0);
        worldMap.setTime(0);
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Order(1)
    @Test
    void testPermission() {
        TimeCommand command = new TimeCommand();
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);

        manager.teleportToServerSpawn(player1);
        manager.teleportToServerSpawn(player2);
        manager.teleportToServerSpawn(player3);
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

        manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0));
        manager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0));
        manager.teleportPlayer(player3, new Location(worldLobby, 0, 0, 0));
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

        Assertions.assertTrue(manager.canTeleportTo(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.canTeleportTo(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(command.canRun(player1));
        Assertions.assertTrue(command.canRun(player2));
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();
    }

    @Order(2)
    @Test
    void testTimeCommand() {
        TimeCommand command = new TimeCommand();
        String label = command.getAliases().get(0);
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        Assertions.assertTrue(manager.canTeleportTo(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.canTeleportTo(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));

        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"1", "2"}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"abc"}));
        Assertions.assertFalse(command.runConstruction(player2, label, new String[]{"-1"}));

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"1"}));
        player1.assertSaid("Set map world time to 1");
        Assertions.assertEquals(1, worldMap.getTime());
        Assertions.assertEquals(0, worldLobby.getTime());
        Assertions.assertEquals(0, world.getTime());
        player1.assertNoMoreSaid();
        player2.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"24001"}));
        player2.assertSaid("Set map world time to 24001");
        Assertions.assertEquals(1, worldMap.getTime());
        Assertions.assertEquals(0, worldLobby.getTime());
        Assertions.assertEquals(0, world.getTime());
        player2.assertNoMoreSaid();
        player1.assertNoMoreSaid();

        worldLobby.setTime(1);
        world.setTime(1);
        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"24000"}));
        player2.assertSaid("Set map world time to 24000");
        Assertions.assertEquals(0, worldMap.getTime());
        Assertions.assertEquals(1, worldLobby.getTime());
        Assertions.assertEquals(1, world.getTime());
        player2.assertNoMoreSaid();
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"23999"}));
        player2.assertSaid("Set map world time to 23999");
        Assertions.assertEquals(23999, worldMap.getTime());
        Assertions.assertEquals(1, worldLobby.getTime());
        Assertions.assertEquals(1, world.getTime());
        player2.assertNoMoreSaid();
        player1.assertNoMoreSaid();
    }
}
