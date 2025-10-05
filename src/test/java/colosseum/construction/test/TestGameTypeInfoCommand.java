package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.GameTypeUtils;
import colosseum.construction.command.GameTypeInfoCommand;
import colosseum.construction.manager.GameTypeInfoManager;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite1;
import colosseum.utility.arcade.GameType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.function.BiConsumer;

class TestGameTypeInfoCommand {
    private static DummySite plugin;
    private static PlayerMock player;

    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    void setup() {
        plugin = new DummySite1(tempPluginDataDir);
        player = MockBukkit.getMock().addPlayer();
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        plugin.disable();
        MockBukkit.unload();
    }

    @Order(0)
    @Test
    void testPermission() {
        GameTypeInfoCommand command = new GameTypeInfoCommand();
        Assertions.assertTrue(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(command.canRun(player));
    }

    @Order(1)
    @Test
    void testInvalidCases() {
        GameTypeInfoCommand command = new GameTypeInfoCommand();
        String label = command.getAliases().get(0);

        BiConsumer<String[], Boolean> assertSaidValidGameTypes = (args, boo) -> {
            if (boo) {
                Assertions.assertTrue(command.runConstruction(player, label, args));
            } else {
                Assertions.assertFalse(command.runConstruction(player, label, args));
                return;
            }
            String message = player.nextMessage();
            Assertions.assertNotNull(message);
            Assertions.assertTrue(message.startsWith("§cValid game types:"));
            Assertions.assertNull(player.nextMessage());
        };

        assertSaidValidGameTypes.accept(new String[]{}, false);
        assertSaidValidGameTypes.accept(new String[]{"invalid", "invalid", "invalid", "invalid"}, false);
        assertSaidValidGameTypes.accept(new String[]{"add", "invalid", "invalid", "invalid"}, true);
        assertSaidValidGameTypes.accept(new String[]{"clear", "invalid", "invalid", "invalid"}, false);
        assertSaidValidGameTypes.accept(new String[]{"clear", GameTypeUtils.getGameTypes().get(0).name(), "invalid", "invalid"}, false);
        assertSaidValidGameTypes.accept(new String[]{"delete", "invalid", "invalid", "invalid"}, false);
        assertSaidValidGameTypes.accept(new String[]{"delete", GameTypeUtils.getGameTypes().get(0).name(), "invalid", "invalid"}, false);

        assertSaidValidGameTypes.accept(new String[]{GameType.None.name()}, true);
        assertSaidValidGameTypes.accept(new String[]{"invalid"}, true);
        assertSaidValidGameTypes.accept(new String[]{"add"}, true);
        assertSaidValidGameTypes.accept(new String[]{"clear"}, true);
        assertSaidValidGameTypes.accept(new String[]{"delete"}, true);

        assertSaidValidGameTypes.accept(new String[]{"add", GameType.None.name()}, false);
        assertSaidValidGameTypes.accept(new String[]{"add", "invalid"}, false);
        assertSaidValidGameTypes.accept(new String[]{"clear", GameType.None.name()}, true);
        assertSaidValidGameTypes.accept(new String[]{"clear", "invalid"}, true);
        assertSaidValidGameTypes.accept(new String[]{"delete", GameType.None.name()}, false);
        assertSaidValidGameTypes.accept(new String[]{"delete", "invalid"}, false);

        assertSaidValidGameTypes.accept(new String[]{"add", GameType.None.name(), "line"}, true);
        assertSaidValidGameTypes.accept(new String[]{"add", "invalid", "line"}, true);
        assertSaidValidGameTypes.accept(new String[]{"add", GameType.None.name(), "line", "1"}, true);
        assertSaidValidGameTypes.accept(new String[]{"add", "invalid", "line", "1"}, true);
        assertSaidValidGameTypes.accept(new String[]{"delete", GameType.None.name(), "1"}, true);
        assertSaidValidGameTypes.accept(new String[]{"delete", "invalid", "1"}, true);
        assertSaidValidGameTypes.accept(new String[]{"delete", GameTypeUtils.getGameTypes().get(0).name(), "nan"}, false);
        assertSaidValidGameTypes.accept(new String[]{"delete", GameTypeUtils.getGameTypes().get(0).name(), "a"}, false);
    }

    @Order(2)
    @Test
    void testValidCases() {
        GameTypeInfoCommand command = new GameTypeInfoCommand();
        String label = command.getAliases().get(0);

        for (GameType gameType : GameTypeUtils.getGameTypes()) {
            command.runConstruction(player, label, new String[]{gameType.name()});
            player.assertSaid("§cNo info found for §e" + gameType.name());

            String line1 = "test line 1!@#$%^&*()_+{}|:,./<>?;'\"[]{}";
            String line2 = "test line 2!@#$%^&*()_+{}|:,./<>?;'\"[]{}";
            String line3 = "test line 3!@#$%^&*()_+{}|:,./<>?;'\"[]{}";
            String line4 = "test line 4!@#$%^&*()_+{}|:,./<>?;'\"[]{}";
            String line5 = "test line 5!@#$%^&*()_+{}|:,./<>?;'\"[]{}";
            command.runConstruction(player, label, new String[]{"add", gameType.name(), line1});
            command.runConstruction(player, label, new String[]{"add", gameType.name(), line2});
            command.runConstruction(player, label, new String[]{"add", gameType.name(), line3});
            command.runConstruction(player, label, new String[]{"add", gameType.name(), line4});
            command.runConstruction(player, label, new String[]{"add", gameType.name(), line5});
            player.assertSaid("Add new gametype info content to " + gameType.name() + ": " + line1);
            player.assertSaid("Add new gametype info content to " + gameType.name() + ": " + line2);
            player.assertSaid("Add new gametype info content to " + gameType.name() + ": " + line3);
            player.assertSaid("Add new gametype info content to " + gameType.name() + ": " + line4);
            player.assertSaid("Add new gametype info content to " + gameType.name() + ": " + line5);

            ConstructionSiteProvider.getSite().getManager(GameTypeInfoManager.class).unregister();
            ConstructionSiteProvider.getSite().getManager(GameTypeInfoManager.class).register();

            command.runConstruction(player, label, new String[]{gameType.name()});
            player.assertSaid(line1);
            player.assertSaid(line2);
            player.assertSaid(line3);
            player.assertSaid(line4);
            player.assertSaid(line5);

            Assertions.assertFalse(command.runConstruction(player, label, new String[]{"delete", gameType.name(), "0"}));
            Assertions.assertFalse(command.runConstruction(player, label, new String[]{"delete", gameType.name(), "6"}));
            Assertions.assertTrue(command.runConstruction(player, label, new String[]{"delete", gameType.name(), "1"}));
            Assertions.assertFalse(command.runConstruction(player, label, new String[]{"delete", gameType.name(), "4", "invalid"}));
            Assertions.assertTrue(command.runConstruction(player, label, new String[]{"delete", gameType.name(), "4"}));
            player.assertSaid("Remove gametype info from " + gameType.name() + " at line 1");
            player.assertSaid("Remove gametype info from " + gameType.name() + " at line 4");
            command.runConstruction(player, label, new String[]{gameType.name()});
            player.assertSaid(line2);
            player.assertSaid(line3);
            player.assertSaid(line4);

            command.runConstruction(player, label, new String[]{"clear", gameType.name()});
            player.assertSaid("Clear gametype info for " + gameType.name());
            command.runConstruction(player, label, new String[]{gameType.name()});
            player.assertSaid("§cNo info found for §e" + gameType.name());
        }
    }
}
