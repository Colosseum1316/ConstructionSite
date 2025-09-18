package colosseum.construction.test.dummies.manager;

import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.ManagerDependency;

@ManagerDependency({DummyManager3.class, DummyManager7.class})
public class DummyManager4 extends ConstructionSiteManager {
    public DummyManager4() {
        super("Dummy 4");
    }
}
