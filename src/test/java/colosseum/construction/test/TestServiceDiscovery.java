package colosseum.construction.test;

import colosseum.construction.PermissionUtils;
import colosseum.construction.PluginUtils;
import colosseum.construction.command.AbstractOpCommand;
import colosseum.construction.command.ConstructionSiteCommand;
import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite1;
import colosseum.construction.test.dummies.manager.DummyManager1;
import colosseum.construction.test.dummies.manager.DummyManager2;
import colosseum.construction.test.dummies.manager.DummyManager3;
import colosseum.construction.test.dummies.manager.DummyManager4;
import colosseum.construction.test.dummies.manager.DummyManager5;
import colosseum.construction.test.dummies.manager.DummyManager6;
import colosseum.construction.test.dummies.manager.DummyManager7;
import colosseum.construction.test.dummies.manager.DummyManager8;
import org.bukkit.permissions.PermissionDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

class TestServiceDiscovery {
    private DummySite plugin;

    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    void setup() {
        tearDown();
        plugin = new DummySite1(tempPluginDataDir);
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Test
    void testServiceLoader() {
        ServiceLoader<ConstructionSiteCommand> providers = ServiceLoader.load(ConstructionSiteCommand.class, ConstructionSiteCommand.class.getClassLoader());
        Assertions.assertEquals(21, StreamSupport.stream(providers.spliterator(), false).count());
        for (Object provider : providers) {
            Class<? extends ConstructionSiteCommand> providerClass = provider.getClass().asSubclass(ConstructionSiteCommand.class);
            Assertions.assertDoesNotThrow(() -> {
                ConstructionSiteCommand c = providerClass.getDeclaredConstructor().newInstance();
                Assertions.assertEquals(PermissionUtils.getPermissionString(c), PermissionUtils.getPermission(c).getName());
                Assertions.assertEquals("colosseum.construction." + c.getAliases().get(0), PermissionUtils.getPermissionString(c));
                Assertions.assertEquals(c instanceof AbstractOpCommand ? PermissionDefault.OP : PermissionDefault.TRUE, PermissionUtils.getPermission(c).getDefault());
            });
        }

        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertEquals(7, PluginUtils.discoverManagers(ServiceLoader.load(ConstructionSiteManager.class, ConstructionSiteManager.class.getClassLoader())).size());
        });
    }

    @Test
    void testConstructionSiteManagerDiscoveryCyclingGraph() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            PluginUtils.discoverManagers(Arrays.asList(DummyManager1.class, DummyManager2.class));
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructionSiteManagerDiscovery() {
        Assertions.assertDoesNotThrow(() -> {
            // 5 -> 3 -> 4
            // ^    ^    ^
            // 8 -> 7 ---|
            List<Class<? extends ConstructionSiteManager>> discovery = PluginUtils.discoverManagers(Arrays.asList(DummyManager3.class, DummyManager4.class, DummyManager5.class, DummyManager6.class, DummyManager7.class, DummyManager8.class));
            Assertions.assertEquals(6, discovery.size());
            final int index3 = discovery.indexOf(DummyManager3.class);
            final int index4 = discovery.indexOf(DummyManager4.class);
            final int index5 = discovery.indexOf(DummyManager5.class);
            final int index7 = discovery.indexOf(DummyManager7.class);
            final int index8 = discovery.indexOf(DummyManager8.class);
            Assertions.assertTrue(index5 < index3 && index3 < index4);
            Assertions.assertTrue(index8 < index7 && index7 < index3 && index7 < index4);
            Assertions.assertTrue(index8 < index5);
        });
    }
}
