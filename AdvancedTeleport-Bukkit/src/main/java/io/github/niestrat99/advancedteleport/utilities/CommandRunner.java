package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandRunner {

    public static void runCommandsOnWarmUp(final @NotNull String teleportCommand, final @NotNull Location location,
                                           final @NotNull Player executor, final @NotNull Placeholder... placeholders) {

        runCommands(MainConfig.get().COMMANDS_ON_WARM_UP,
                teleportCommand,
                executor,
                addLocationPlaceholders(placeholders, location));
    }

    public static void runCommandsOnTeleport(final @NotNull String teleportCommand, final @NotNull Location location,
                                             final @NotNull Player executor, final @NotNull Placeholder... placeholders) {

        runCommands(
                MainConfig.get().COMMANDS_ON_TELEPORT,
                teleportCommand,
                executor,
                addLocationPlaceholders(placeholders, location));
    }

    public static void runCommandsOnCooldownExpire(final @NotNull String teleportCommand, final @NotNull Player affectedPlayer,
                                                   final @NotNull Placeholder... placeholders) {

        runCommands(
                MainConfig.get().COMMANDS_ON_COOLDOWN_EXPIRE,
                teleportCommand,
                affectedPlayer,
                placeholders);
    }

    public static void runCommandsOnCancel(final @NotNull String teleportCommand, final @NotNull Player affectedPlayer,
                                           final @NotNull Placeholder... placeholders) {

        runCommands(MainConfig.get().COMMANDS_ON_CANCEL,
                teleportCommand,
                affectedPlayer,
                placeholders);
    }

    public static void runCommands(final @NotNull MainConfig.PerCommandOption<Object> configuration,
                                   final @NotNull String teleportCommand,
                                   final @NotNull Player executor, final @NotNull Placeholder... placeholders) {

        final Placeholder[] finalPlaceholders = addPlaceholders(
                placeholders,
                new Placeholder("player", executor.getName()),
                new Placeholder("command", teleportCommand));

        Object commandList = configuration.valueOf(teleportCommand).get();
        if (commandList instanceof String command) {
            runCommand(executor, buildCommand(command, finalPlaceholders));
        } else if (commandList instanceof List<?> commands) {

            for (Object rawCommand : commands) {
                if (rawCommand instanceof String command) {
                    runCommand(executor, buildCommand(command, finalPlaceholders));
                }
            }
        }
    }

    private static Command buildCommand(final @NotNull String rawCommand, final @NotNull Placeholder[] placeholders) {
        String buildingCommand = rawCommand;
        Sender sender = Sender.PLAYER;
        if (buildingCommand.startsWith("server;")) {
            buildingCommand = buildingCommand.substring("server;".length());
            sender = Sender.SERVER;
        } else if (buildingCommand.startsWith("player;")) {
            buildingCommand = buildingCommand.substring("player;".length());
        }

        for (Placeholder placeholder : placeholders) {
            buildingCommand = buildingCommand.replace("{" + placeholder.placeholder + "}", String.valueOf(placeholder.value));
        }
        return new Command(buildingCommand, sender);
    }

    private static void runCommand(final @NotNull Player executor, final @NotNull Command command) {
        if (command.sender == Sender.PLAYER) {
            if (!executor.performCommand(command.command)) {
                CoreClass.getInstance().getLogger().warning("Command /" + command.command + " run by " + executor.getName() + " failed (returned false).");
            }
        } else {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.command)) {
                CoreClass.getInstance().getLogger().warning("Command /" + command.command + " run in the console failed (returned false).");
            }
        }
    }

    private static Placeholder[] addPlaceholders(final @NotNull Placeholder[] currentPlaceholders,
                                          final @NotNull Placeholder... newPlaceholders) {
        Placeholder[] newArray = new Placeholder[currentPlaceholders.length + newPlaceholders.length];
        System.arraycopy(currentPlaceholders, 0, newArray, 0, currentPlaceholders.length);
        System.arraycopy(newPlaceholders, 0, newArray, currentPlaceholders.length, newPlaceholders.length);

        return newArray;
    }

    private static Placeholder[] addLocationPlaceholders(final @NotNull Placeholder[] currentPlaceholders,
                                                         final @NotNull Location location) {
        return addPlaceholders(
                currentPlaceholders,
                new Placeholder("x", location.getX()),
                new Placeholder("y", location.getY()),
                new Placeholder("z", location.getZ()),
                new Placeholder("block_x", location.getBlockX()),
                new Placeholder("block_y", location.getBlockY()),
                new Placeholder("block_z", location.getBlockZ()),
                new Placeholder("yaw", location.getYaw()),
                new Placeholder("pitch", location.getPitch()),
                new Placeholder("world", location.getWorld().getName()));
    }


    private enum Sender {
        PLAYER,
        SERVER
    }

    public record Placeholder(String placeholder, Object value) {

    }

    private record Command(String command, Sender sender) {
    }
}
