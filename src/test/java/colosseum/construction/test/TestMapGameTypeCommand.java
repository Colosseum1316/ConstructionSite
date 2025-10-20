package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.command.MapGameTypeCommand;
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
import colosseum.utility.arcade.GameType;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

class TestMapGameTypeCommand {
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
                MAP_NAME:MAPINFO 1234999GAMETYPE
                MAP_AUTHOR:MAPAUTHOR
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
        MapGameTypeCommand command = new MapGameTypeCommand();
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
        MapGameTypeCommand command = new MapGameTypeCommand();
        TeleportManager teleportManager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        MapDataManager mapDataManager = ConstructionSiteProvider.getSite().getManager(MapDataManager.class);
        String label = command.getAliases().get(0);

        Assertions.assertTrue(teleportManager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0)));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player1, label, new String[]{"", ""}));
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"None"}));
        String message = player1.nextMessage();
        Assertions.assertTrue(message.startsWith("§cValid game types:"));
        Assertions.assertFalse(message.contains("None"));
        player1.assertNoMoreSaid();

        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{"abc123"}));
        message = player1.nextMessage();
        Assertions.assertTrue(message.startsWith("§cValid game types:"));
        player1.assertNoMoreSaid();

        Assertions.assertEquals(GameType.DragonEscape, mapDataManager.get(worldMap).getMapGameType());
        Assertions.assertTrue(command.runConstruction(player1, label, new String[]{GameType.NanoGames.name()}));
        player1.assertSaid("Map MAPINFO 1234999GAMETYPE: Set GameType to " + GameType.NanoGames.name());
        player1.assertNoMoreSaid();
        Assertions.assertEquals(GameType.NanoGames, mapDataManager.get(worldMap).getMapGameType());
        Assertions.assertTrue(((MutableMapData) mapDataManager.get(worldMap)).write());
        MapData data = Utils.readMapData(worldMap, WorldUtils.getWorldFolder(worldMap));
        Assertions.assertEquals(GameType.NanoGames, data.getMapGameType());
    }
}
