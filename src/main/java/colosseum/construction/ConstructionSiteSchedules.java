package colosseum.construction;

public interface ConstructionSiteSchedules {
    <T> T schedule(Runnable runnable, Class<T> clazz);
    <T> T schedule(Runnable runnable, Class<T> clazz, long delay);
    <T> T schedule(Runnable runnable, Class<T> clazz, long delay, long period);
    <T> T scheduleAsync(Runnable runnable, Class<T> clazz);
}
