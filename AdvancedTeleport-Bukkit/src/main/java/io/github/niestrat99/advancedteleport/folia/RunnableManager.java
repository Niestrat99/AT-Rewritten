package io.github.niestrat99.advancedteleport.folia;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.folia.schedulers.FoliaRunnable;
import io.github.niestrat99.advancedteleport.folia.schedulers.NormalBukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RunnableManager {

    public static CancellableRunnable setupRunnerPeriod(Entity entity, Consumer<CancellableRunnable> runnable, Runnable retired, long delay, long period) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTaskTimer(CoreClass.getInstance(), run::start, delay, period),
                (run) -> entity.getScheduler().runAtFixedRate(CoreClass.getInstance(), run::start, retired, delay, period));
    }

    public static CancellableRunnable setupRunnerDelayed(Entity entity, Consumer<CancellableRunnable> runnable, Runnable retired, long delay) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTaskLater(CoreClass.getInstance(), run::start, delay),
                (run) -> entity.getScheduler().runDelayed(CoreClass.getInstance(), run::start, retired, delay));
    }

    public static CancellableRunnable setupRunnerDelayed(Runnable runnable, long delay) {

        return setupRunnerDelayed(task -> runnable.run(), delay);
    }

    public static CancellableRunnable setupRunnerDelayed(Consumer<CancellableRunnable> runnable, long delay) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTaskLater(CoreClass.getInstance(), run::start, delay),
                (run) -> Bukkit.getGlobalRegionScheduler().runDelayed(CoreClass.getInstance(), run::start, delay));
    }

    public static CancellableRunnable setupRunnerDelayedAsync(Consumer<CancellableRunnable> runnable, long delay) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTaskLaterAsynchronously(CoreClass.getInstance(), run::start, delay),
                (run) -> Bukkit.getAsyncScheduler().runDelayed(CoreClass.getInstance(), run::start, delay * 50, TimeUnit.MILLISECONDS));
    }

    public static CancellableRunnable setupRunnerPeriod(Consumer<CancellableRunnable> runnable, long delay, long period) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTaskTimer(CoreClass.getInstance(), run::start, delay, period),
                (run) -> Bukkit.getGlobalRegionScheduler().runAtFixedRate(CoreClass.getInstance(), run::start, delay, period));
    }

    public static CancellableRunnable setupRunnerPeriodAsync(Runnable runnable, long delay, long period) {

        return setupRunnerPeriodAsync(task -> runnable.run(), delay, period);
    }

    public static CancellableRunnable setupRunnerPeriodAsync(Consumer<CancellableRunnable> runnable, long delay, long period) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTaskTimerAsynchronously(CoreClass.getInstance(), run::start, delay, period),
                (run) -> Bukkit.getAsyncScheduler().runAtFixedRate(CoreClass.getInstance(), run::start, delay * 50, period * 50, TimeUnit.MILLISECONDS));
    }

    public static CancellableRunnable setupRunnerAsync(Runnable runnable) {

        return setupRunnerAsync(task -> runnable.run());
    }

    public static CancellableRunnable setupRunnerAsync(Consumer<CancellableRunnable> runnable) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), run::start),
                (run) -> Bukkit.getAsyncScheduler().runNow(CoreClass.getInstance(), run::start));
    }

    public static CancellableRunnable setupRunner(Runnable runnable) {

        return setupRunner(task -> runnable.run());
    }

    public static CancellableRunnable setupRunner(Location location, Runnable runnable) {
        return setupRunner(location, task -> runnable.run());
    }

    public static CancellableRunnable setupRunner(Entity entity, Runnable runnable, Runnable retired) {
        return setupRunner(entity, task -> runnable.run(), retired);
    }

    public static CancellableRunnable setupRunner(Consumer<CancellableRunnable> runnable) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), run::start),
                (run) -> Bukkit.getGlobalRegionScheduler().run(CoreClass.getInstance(), run::start));
    }

    public static CancellableRunnable setupRunner(Location location, Consumer<CancellableRunnable> runnable) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), run::start),
                (run) -> Bukkit.getRegionScheduler().execute(CoreClass.getInstance(), location, run));
    }

    public static CancellableRunnable setupRunner(Entity entity, Consumer<CancellableRunnable> runnable, Runnable retired) {

        return run(runnable,
                (run) -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), run::start),
                (run) -> entity.getScheduler().run(CoreClass.getInstance(), run::start, retired));
    }

    private static CancellableRunnable run(
            @NotNull Consumer<CancellableRunnable> runnable,
            @NotNull Consumer<NormalBukkitRunnable> bukkitRunnable,
            @NotNull Consumer<FoliaRunnable> foliaRunnable
    ) {

        if (!isFolia()) {
            NormalBukkitRunnable coolRunnable = new NormalBukkitRunnable(runnable);
            bukkitRunnable.accept(coolRunnable);
            return coolRunnable;
        }

        FoliaRunnable coolRunnable = new FoliaRunnable(runnable);
        foliaRunnable.accept(coolRunnable);
        return coolRunnable;
    }

    public static boolean isFolia() {

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            return true;
        } catch (NoClassDefFoundError | ClassNotFoundException ignored) {
            return false;
        }
    }
}
