package colosseum.construction.test;

import colosseum.construction.PluginUtils;
import colosseum.construction.command.ConstructionSiteCommand;
import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.test.dummies.manager.DummyManager1;
import colosseum.construction.test.dummies.manager.DummyManager2;
import colosseum.construction.test.dummies.manager.DummyManager3;
import colosseum.construction.test.dummies.manager.DummyManager4;
import colosseum.construction.test.dummies.manager.DummyManager5;
import colosseum.construction.test.dummies.manager.DummyManager6;
import colosseum.construction.test.dummies.manager.DummyManager7;
import colosseum.construction.test.dummies.manager.DummyManager8;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;

class TestServiceLoader {
    @Test
    void testServiceLoader() {
        ServiceLoader<ConstructionSiteCommand> loader = ServiceLoader.load(ConstructionSiteCommand.class, ConstructionSiteCommand.class.getClassLoader());
        Assertions.assertEquals(23, loader.stream().toList().size());
        for (Object provider : loader) {
            Class<? extends ConstructionSiteCommand> providerClass = provider.getClass().asSubclass(ConstructionSiteCommand.class);
            Assertions.assertDoesNotThrow(() -> {
                providerClass.getDeclaredConstructor().newInstance();
            });
        }

        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertEquals(8, PluginUtils.discoverManagers().size());
        });
    }

    @Test
    void testConstructionSiteManagerDiscoveryCyclingGraph() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            PluginUtils.discoverManagers(List.of(DummyManager1.class, DummyManager2.class));
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructionSiteManagerDiscovery() {
        Assertions.assertDoesNotThrow(() -> {
            // 5 -> 3 -> 4
            // ^    ^    ^
            // 8 -> 7 ---|
            List<Class<? extends ConstructionSiteManager>> discovery = PluginUtils.discoverManagers(List.of(DummyManager3.class, DummyManager4.class, DummyManager5.class, DummyManager6.class, DummyManager7.class, DummyManager8.class));
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
