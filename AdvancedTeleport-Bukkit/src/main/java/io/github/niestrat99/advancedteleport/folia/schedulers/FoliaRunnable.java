package io.github.niestrat99.advancedteleport.folia.schedulers;

import io.github.niestrat99.advancedteleport.folia.CancellableRunnable;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FoliaRunnable extends CancellableRunnable {

    private @Nullable ScheduledTask task;

    public FoliaRunnable(@NotNull Consumer<CancellableRunnable> runnable) {
        super(runnable);
    }

    public FoliaRunnable(@NotNull Runnable runnable) {
        super(runnable);
    }

    public void start(@NotNull ScheduledTask task) {
        this.task = task;
        run();
    }

    @Override
    public void cancel() {
        if (task != null) this.task.cancel();
    }
}
