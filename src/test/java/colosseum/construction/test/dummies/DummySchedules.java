package colosseum.construction.test.dummies;

import colosseum.construction.ConstructionSiteSchedulesImpl;

public final class DummySchedules extends ConstructionSiteSchedulesImpl {
    @Override
    public <T> T scheduleAsync(Runnable runnable, Class<T> clazz) {
        if (!clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        runnable.run();
        return null;
    }
}
