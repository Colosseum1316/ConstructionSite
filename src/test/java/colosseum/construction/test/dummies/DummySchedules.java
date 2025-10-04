package colosseum.construction.test.dummies;

import colosseum.construction.ConstructionSiteSchedules;

public final class DummySchedules implements ConstructionSiteSchedules {
    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz) {
        if (!clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        runnable.run();
        return null;
    }

    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz, long delay) {
        if (!clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        runnable.run();
        return null;
    }

    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz, long delay, long period) {
        if (!clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        runnable.run();
        return null;
    }

    @Override
    public <T> T scheduleAsync(Runnable runnable, Class<T> clazz) {
        if (!clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        runnable.run();
        return null;
    }
}
