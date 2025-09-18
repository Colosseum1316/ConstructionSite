package colosseum.construction.test.dummies.manager;

import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.ManagerDependency;

@ManagerDependency(DummyManager2.class)
public class DummyManager1 extends ConstructionSiteManager {
    public DummyManager1() {
        super("Dummy 1");
    }
}
