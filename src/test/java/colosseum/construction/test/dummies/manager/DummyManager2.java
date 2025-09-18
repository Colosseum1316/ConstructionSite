package colosseum.construction.test.dummies.manager;

import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.ManagerDependency;

@ManagerDependency(DummyManager1.class)
public class DummyManager2 extends ConstructionSiteManager {
    public DummyManager2() {
        super("Dummy 2");
    }
}
