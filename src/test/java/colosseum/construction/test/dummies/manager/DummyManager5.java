package colosseum.construction.test.dummies.manager;

import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.ManagerDependency;

@ManagerDependency(DummyManager8.class)
public class DummyManager5 extends ConstructionSiteManager {
    public DummyManager5() {
        super("Dummy 5");
    }
}
