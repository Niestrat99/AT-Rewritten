package io.github.niestrat99.advancedteleport.utilities;

import org.jetbrains.annotations.Nullable;

public class Pair<S, T> {

    private final @Nullable S s;
    private final @Nullable T t;

    public Pair(final @Nullable S s, final @Nullable T t) {
        this.s = s;
        this.t = t;
    }

    public S fst() {
        return this.s;
    }

    public T snd() {
        return this.t;
    }
}
