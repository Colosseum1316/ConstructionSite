package colosseum.construction;

public interface ConstructionSiteSchedules {
    <T> T schedule(Runnable runnable, Class<T> clazz);

    <T> T schedule(Runnable runnable, Class<T> clazz, long delay);

    <T> T schedule(Runnable runnable, Class<T> clazz, long delay, long period);

    <T> T scheduleAsync(Runnable runnable, Class<T> clazz);

    default void schedule(Runnable runnable) {
        schedule(runnable, Void.class);
    }

    default void schedule(Runnable runnable, long delay) {
        schedule(runnable, Void.class, delay);
    }

    default void schedule(Runnable runnable, long delay, long period) {
        schedule(runnable, Void.class, delay, period);
    }

    default void scheduleAsync(Runnable runnable) {
        scheduleAsync(runnable, Void.class);
    }
}
