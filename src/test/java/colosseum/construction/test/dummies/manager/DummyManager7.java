package colosseum.construction.test.dummies.manager;

import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.ManagerDependency;

@ManagerDependency(DummyManager8.class)
public class DummyManager7 extends ConstructionSiteManager {
    public DummyManager7() {
        super("Dummy 7");
    }
}
