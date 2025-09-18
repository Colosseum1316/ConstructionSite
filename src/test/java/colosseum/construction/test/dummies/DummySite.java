package colosseum.construction.test.dummies;

import colosseum.construction.ConstructionSite;

public interface DummySite extends ConstructionSite {
    void setup();

    void teardown();
}
