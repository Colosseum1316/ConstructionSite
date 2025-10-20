package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.vanilla.DifficultyCommand;
import colosseum.construction.manager.TeleportManager;
import colosseum.construction.test.dummies.ConstructionSitePlayerMock;
import colosseum.construction.test.dummies.ConstructionSiteServerMock;
import colosseum.construction.test.dummies.ConstructionSiteWorldMock;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite3;
import colosseum.utility.WorldMapConstants;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

class TestDifficultyCommand {
    private DummySite plugin;
    private ConstructionSiteWorldMock world;
    private ConstructionSiteWorldMock worldLobby;
    private ConstructionSiteWorldMock worldMap;

    @TempDir
    static File tempWorldContainer;
    @TempDir
    static File tempPluginDataDir;

    private static final String uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf";
    private ConstructionSitePlayerMock player1;
    private static final String uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693";
    private ConstructionSitePlayerMock player2;

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
                MAP_NAME:Test map3 difficulty
                MAP_AUTHOR:Test author4
                GAME_TYPE:None
                ADMIN_LIST:%s
                """.trim(), uuid2));
        plugin.enable();

        world.setDifficulty(Difficulty.PEACEFUL);
        worldMap.setDifficulty(Difficulty.PEACEFUL);
        worldLobby.setDifficulty(Difficulty.PEACEFUL);
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Order(1)
    @Test
    void testPermission() {
        DifficultyCommand command = new DifficultyCommand();
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);

        Assertions.assertTrue(manager.teleportToServerSpawn(player1));
        Assertions.assertTrue(manager.teleportToServerSpawn(player2));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        player1.assertSaid("§cYou are in \"world\"!");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.canRun(player2));
        player2.assertSaid("§cYou are in \"world\"!");
        player2.assertNoMoreSaid();

        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        player1.assertSaid("§cYou are in \"world_lobby\"!");
        player1.assertNoMoreSaid();
        Assertions.assertFalse(command.canRun(player2));
        player2.assertSaid("§cYou are in \"world_lobby\"!");
        player2.assertNoMoreSaid();

        Assertions.assertFalse(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertTrue(manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player1));
        player1.assertSaid("§cYou are in \"world_lobby\"!");
        player1.assertNoMoreSaid();
        Assertions.assertTrue(command.canRun(player2));
        player2.assertNoMoreSaid();

        player1.setOp(true);
        Assertions.assertTrue(manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(command.canRun(player1));
        player1.assertNoMoreSaid();
        player1.setOp(false);
    }

    @Order(2)
    @Test
    void testNumericDifficulties() {
        DifficultyCommand command = new DifficultyCommand();
        String label = command.getAliases().get(0);
        Assertions.assertFalse(command.runConstruction(player2, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player2, label, new String[]{"1", "1"}));
        Assertions.assertFalse(command.runConstruction(player2, label, new String[]{"9"}));
        Assertions.assertFalse(command.runConstruction(player2, label, new String[]{"-1"}));

        world.setDifficulty(Difficulty.NORMAL);
        worldMap.setDifficulty(Difficulty.NORMAL);
        worldLobby.setDifficulty(Difficulty.NORMAL);

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"0"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to PEACEFUL");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.PEACEFUL, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.PEACEFUL, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.PEACEFUL, worldMap.getDifficulty());

        world.setDifficulty(Difficulty.PEACEFUL);
        worldMap.setDifficulty(Difficulty.PEACEFUL);
        worldLobby.setDifficulty(Difficulty.PEACEFUL);

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"1"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to EASY");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.EASY, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.EASY, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.EASY, worldMap.getDifficulty());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"2"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to NORMAL");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.NORMAL, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.NORMAL, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.NORMAL, worldMap.getDifficulty());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"3"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to HARD");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.HARD, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.HARD, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.HARD, worldMap.getDifficulty());
    }

    @Order(3)
    @Test
    void testDifficultiesByName() {
        DifficultyCommand command = new DifficultyCommand();
        String label = command.getAliases().get(0);

        world.setDifficulty(Difficulty.NORMAL);
        worldMap.setDifficulty(Difficulty.NORMAL);
        worldLobby.setDifficulty(Difficulty.NORMAL);

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"peaceFUL"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to PEACEFUL");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.PEACEFUL, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.PEACEFUL, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.PEACEFUL, worldMap.getDifficulty());

        world.setDifficulty(Difficulty.PEACEFUL);
        worldMap.setDifficulty(Difficulty.PEACEFUL);
        worldLobby.setDifficulty(Difficulty.PEACEFUL);

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"Easy"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to EASY");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.EASY, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.EASY, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.EASY, worldMap.getDifficulty());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"normal"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to NORMAL");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.NORMAL, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.NORMAL, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.NORMAL, worldMap.getDifficulty());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"HaRd"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to HARD");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.HARD, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.HARD, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.HARD, worldMap.getDifficulty());
    }

    @Order(4)
    @Test
    void testDifficultiesByPrefix() {
        DifficultyCommand command = new DifficultyCommand();
        String label = command.getAliases().get(0);

        world.setDifficulty(Difficulty.NORMAL);
        worldMap.setDifficulty(Difficulty.NORMAL);
        worldLobby.setDifficulty(Difficulty.NORMAL);

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"p"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to PEACEFUL");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.PEACEFUL, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.PEACEFUL, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.PEACEFUL, worldMap.getDifficulty());

        world.setDifficulty(Difficulty.PEACEFUL);
        worldMap.setDifficulty(Difficulty.PEACEFUL);
        worldLobby.setDifficulty(Difficulty.PEACEFUL);

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"E"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to EASY");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.EASY, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.EASY, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.EASY, worldMap.getDifficulty());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"n"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to NORMAL");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.NORMAL, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.NORMAL, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.NORMAL, worldMap.getDifficulty());

        Assertions.assertTrue(command.runConstruction(player2, label, new String[]{"H"}));
        player2.assertSaid("Set map Test map3 difficulty world difficulty to HARD");
        player2.assertNoMoreSaid();
        Assertions.assertNotEquals(Difficulty.HARD, world.getDifficulty());
        Assertions.assertNotEquals(Difficulty.HARD, worldLobby.getDifficulty());
        Assertions.assertEquals(Difficulty.HARD, worldMap.getDifficulty());
    }
}
