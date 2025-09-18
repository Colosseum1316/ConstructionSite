package colosseum.construction.test.dummies.manager;

import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.ManagerDependency;

@ManagerDependency({DummyManager5.class, DummyManager7.class})
public class DummyManager3 extends ConstructionSiteManager {
    public DummyManager3() {
        super("Dummy 3");
    }
}
