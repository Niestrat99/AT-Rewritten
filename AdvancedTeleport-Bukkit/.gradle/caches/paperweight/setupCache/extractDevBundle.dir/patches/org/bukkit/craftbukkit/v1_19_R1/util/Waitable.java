package org.bukkit.craftbukkit.v1_19_R1.util;

import java.util.concurrent.ExecutionException;

public abstract class Waitable<T> implements Runnable {
    private enum Status {
        WAITING,
        RUNNING,
        FINISHED,
    }
    Throwable t = null;
    T value = null;
    Status status = Status.WAITING;

    @Override
    public final void run() {
        synchronized (this) {
            if (this.status != Status.WAITING) {
                throw new IllegalStateException("Invalid state " + this.status);
            }
            this.status = Status.RUNNING;
        }
        try {
            this.value = this.evaluate();
        } catch (Throwable t) {
            this.t = t;
        } finally {
            synchronized (this) {
                this.status = Status.FINISHED;
                this.notifyAll();
            }
        }
    }

    protected abstract T evaluate();

    public synchronized T get() throws InterruptedException, ExecutionException {
        while (this.status != Status.FINISHED) {
            this.wait();
        }
        if (this.t != null) {
            throw new ExecutionException(this.t);
        }
        return this.value;
    }
}
