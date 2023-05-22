package io.github.niestrat99.advancedteleport.folia;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CancellableRunnable implements Runnable {

    private final @NotNull Consumer<CancellableRunnable> consumer;
    protected boolean cancelled = false;

    public CancellableRunnable(final @NotNull Consumer<CancellableRunnable> consumer) {
        this.consumer = consumer;
    }

    public CancellableRunnable(final @NotNull Runnable runnable) {
        this(task -> runnable.run());
    }

    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run() {
        this.consumer.accept(this);
    }
}
