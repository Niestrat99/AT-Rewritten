package io.github.niestrat99.advancedteleport.commands;

import org.jetbrains.annotations.NotNull;

public abstract class SubATCommand extends ATCommand {

    @Override
    public final boolean getRequiredFeature() {
        return true;
    }

    @Override
    public final @NotNull String getPermission() {
        return "";
    }
}
