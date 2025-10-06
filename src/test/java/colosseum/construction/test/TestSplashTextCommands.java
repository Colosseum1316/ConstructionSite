package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import colosseum.construction.ConstructionSite;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.command.AbstractOpCommand;
import colosseum.construction.command.OpAddSplashTextCommand;
import colosseum.construction.command.OpClearSplashTextCommand;
import colosseum.construction.event.InteractionEvents;
import colosseum.construction.manager.SplashTextManager;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite1;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

class TestSplashTextCommands {
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

    @Order(1)
    @Test
    void testPermission() {
        AbstractOpCommand[] commands = new AbstractOpCommand[] {
                new OpAddSplashTextCommand(),
                new OpClearSplashTextCommand()
        };
        for (AbstractOpCommand command : commands) {
            player.setOp(false);
            Assertions.assertFalse(command.canRun(player));
            Assertions.assertTrue(command.canRun(MockBukkit.getMock().getConsoleSender()));
            player.setOp(true);
            Assertions.assertTrue(command.canRun(player));
            Assertions.assertTrue(command.canRun(MockBukkit.getMock().getConsoleSender()));
        }
    }

    @Order(2)
    @Test
    void testAddAndClearSplashText() {
        player.assertNoMoreSaid();
        player.setOp(true);

        AbstractOpCommand command = new OpAddSplashTextCommand();
        String label = command.getAliases().get(0);

        Assertions.assertFalse(command.runConstruction(player, label, new String[]{}));
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"Line", "1", "of", "splash", "text"}));
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"Line", "2", "&cof", "splash", "text"}));
        player.assertSaid("Add splash text: Line 1 of splash text");
        player.assertSaid("Add splash text: Line 2 &cof splash text");
        player.assertNoMoreSaid();
        ConstructionSite site = ConstructionSiteProvider.getSite();
        List<String> splashText = site.getManager(SplashTextManager.class).getText();
        Assertions.assertLinesMatch(List.of(
                "Line 1 of splash text",
                "Line 2 &cof splash text"
        ), splashText);
        site.getManager(SplashTextManager.class).unregister();
        site.getManager(SplashTextManager.class).register();
        splashText = site.getManager(SplashTextManager.class).getText();
        Assertions.assertLinesMatch(List.of(
                "Line 1 of splash text",
                "Line 2 &cof splash text"
        ), splashText);
        PlayerJoinEvent eventMock = new PlayerJoinEvent(player, "test");
        InteractionEvents listener = new InteractionEvents();
        listener.onPlayerJoinPrintSplashText(eventMock);
        player.assertSaid("Line 1 of splash text");
        player.assertSaid("Line 2 §cof splash text");
        player.assertNoMoreSaid();

        command = new OpClearSplashTextCommand();
        label = command.getAliases().get(0);
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{}));
        player.assertSaid("Clear all splash text.");
        player.assertNoMoreSaid();
        splashText = site.getManager(SplashTextManager.class).getText();
        Assertions.assertEquals(0, splashText.size());
        site.getManager(SplashTextManager.class).unregister();
        site.getManager(SplashTextManager.class).register();
        splashText = site.getManager(SplashTextManager.class).getText();
        Assertions.assertEquals(0, splashText.size());
    }
}
