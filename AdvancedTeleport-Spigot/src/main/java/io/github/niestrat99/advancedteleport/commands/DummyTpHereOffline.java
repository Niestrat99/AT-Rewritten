package io.github.niestrat99.advancedteleport.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class DummyTpHereOffline extends TeleportATCommand{

    @Override
    public @NotNull String getPermission() {
        return "";
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        return false;
    }

    @Override
    public boolean getRequiredFeature() {
        return false;
    }
}
