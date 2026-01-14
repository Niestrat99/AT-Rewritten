package io.github.niestrat99.advancedteleport.api;

import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** Represents a location that has a customisable name attached to it, such as a warp or home. */
public interface NamedLocation {

    /**
     * Gives the name associated with the location in question.
     *
     * @return the name of the location.
     */
    @Contract(pure = true)
    @NotNull
    String getName();

    /**
     * Gets the Bukkit location of the named location.
     *
     * @return the Bukkit location.
     */
    @Contract(pure = true)
    @NotNull
    Location getLocation();
}
