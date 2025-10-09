package colosseum.construction;

import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@NoArgsConstructor
final class ConstructionSiteSchedulesImpl implements ConstructionSiteSchedules {
    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz) {
        if (!clazz.equals(BukkitTask.class) && !clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        BukkitTask task = Bukkit.getScheduler().runTask(ConstructionSiteProvider.getPlugin(), runnable);
        if (clazz.equals(BukkitTask.class)) {
            return (T) task;
        } else {
            return null;
        }
    }

    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz, long delay) {
        if (!clazz.equals(BukkitTask.class) && !clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        BukkitTask task = Bukkit.getScheduler().runTaskLater(ConstructionSiteProvider.getPlugin(), runnable, delay);
        if (clazz.equals(BukkitTask.class)) {
            return (T) task;
        } else {
            return null;
        }
    }

    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz, long delay, long period) {
        if (!clazz.equals(BukkitTask.class) && !clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(ConstructionSiteProvider.getPlugin(), runnable, delay, period);
        if (clazz.equals(BukkitTask.class)) {
            return (T) task;
        } else {
            return null;
        }
    }

    @Override
    public <T> T scheduleAsync(Runnable runnable, Class<T> clazz) {
        if (!clazz.equals(BukkitTask.class) && !clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin(), runnable);
        if (clazz.equals(BukkitTask.class)) {
            return (T) task;
        } else {
            return null;
        }
    }
}
