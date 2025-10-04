package colosseum.construction.test.dummies;

import colosseum.construction.ConstructionSite;

public interface DummySite extends ConstructionSite {
    default void load() {

    }

    void enable();

    void disable();
}
