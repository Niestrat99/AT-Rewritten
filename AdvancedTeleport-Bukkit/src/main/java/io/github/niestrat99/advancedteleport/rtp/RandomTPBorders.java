package io.github.niestrat99.advancedteleport.rtp;

import org.jetbrains.annotations.NotNull;

public record RandomTPBorders(double minX, double maxX, double minZ, double maxZ) {

    public RandomTPBorders minimal(final @NotNull RandomTPBorders otherBorder) {
        return new RandomTPBorders(Math.max(this.minX, otherBorder.minX),
                Math.min(this.maxX, otherBorder.maxX),
                Math.max(this.minZ, otherBorder.minZ),
                Math.min(this.maxZ, otherBorder.maxZ));
    }
}
