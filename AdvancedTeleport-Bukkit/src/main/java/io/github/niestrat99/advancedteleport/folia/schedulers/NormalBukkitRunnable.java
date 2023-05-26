package io.github.niestrat99.advancedteleport.folia.schedulers;

import io.github.niestrat99.advancedteleport.folia.CancellableRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class NormalBukkitRunnable extends CancellableRunnable {

    private @Nullable BukkitTask task;

    public NormalBukkitRunnable(@NotNull Consumer<CancellableRunnable> runnable) {
        super(runnable);
    }

    public NormalBukkitRunnable(@NotNull Runnable runnable) {
        super(runnable);
    }

    public void start(@NotNull BukkitTask task) {
        this.task = task;
        run();
    }

    @Override
    public void cancel() {
        if (task != null) this.task.cancel();
    }
}
