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
        if (clazz.equals(BukkitTask.class)) {
            return (T) Bukkit.getScheduler().runTask(ConstructionSiteProvider.getPlugin(), runnable);
        } else {
            return null;
        }
    }

    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz, long delay) {
        if (!clazz.equals(BukkitTask.class) && !clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        if (clazz.equals(BukkitTask.class)) {
            return (T) Bukkit.getScheduler().runTaskLater(ConstructionSiteProvider.getPlugin(), runnable, delay);
        } else {
            return null;
        }
    }

    @Override
    public <T> T schedule(Runnable runnable, Class<T> clazz, long delay, long period) {
        if (!clazz.equals(BukkitTask.class) && !clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        if (clazz.equals(BukkitTask.class)) {
            return (T) Bukkit.getScheduler().runTaskTimer(ConstructionSiteProvider.getPlugin(), runnable, delay, period);
        } else {
            return null;
        }
    }

    @Override
    public <T> T scheduleAsync(Runnable runnable, Class<T> clazz) {
        if (!clazz.equals(BukkitTask.class) && !clazz.equals(Void.class)) {
            throw new UnsupportedOperationException();
        }
        if (clazz.equals(BukkitTask.class)) {
            return (T) Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin(), runnable);
        } else {
            return null;
        }
    }
}
