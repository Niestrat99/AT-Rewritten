package io.github.niestrat99.advancedteleport.commands.core.map;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.hooks.MapPlugin;
import io.github.niestrat99.advancedteleport.managers.MapAssetManager;
import io.github.niestrat99.advancedteleport.managers.PluginHookManager;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AbstractMapCommand extends SubATCommand {

    private final @NotNull String key;
    private final @NotNull Collection<String> completions;
    private final @NotNull String successKey;

    public AbstractMapCommand(
            @NotNull String key,
            @NotNull String successKey,
            @NotNull Collection<String> completions) {
        this.key = key;
        this.successKey = successKey;
        this.completions = completions;
    }

    public AbstractMapCommand(
            @NotNull String key, @NotNull String successKey, @NotNull String... completions) {
        this(key, successKey, new ArrayList<>(Arrays.asList(completions)));
    }

    @Override
    @Contract
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {

        // If not enough arguments are specified, let the player know
        if (args.length < 3) {
            CustomMessages.sendMessage(sender, "Error.notEnoughArgs");
            return true;
        }

        // Squish together the input
        String input = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        CompletableFuture<Boolean> completableFuture;
        switch (args[0].toLowerCase()) {
            case "warp" -> completableFuture =
                    MetadataSQLManager.get().addWarpMetadata(args[1], key, input);
            case "spawn" -> completableFuture =
                    MetadataSQLManager.get().addSpawnMetadata(args[1], key, input);
            case "home" -> {
                if (args.length < 4) {
                    CustomMessages.sendMessage(sender, "Error.notEnoughArgs");
                    return true;
                }

                input = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                String finalInput = input;

                AdvancedTeleportAPI.getOfflinePlayer(args[1])
                        .whenComplete(
                                (player, err) -> {
                                    if (err != null) {
                                        CustomMessages.sendMessage(
                                                sender, "Error.failedMapIconUpdate");
                                        err.printStackTrace();
                                        return;
                                    }

                                    MetadataSQLManager.get()
                                            .addHomeMetadata(
                                                    args[2], player.getUniqueId(), key, finalInput)
                                            .whenComplete(
                                                    (result, err2) ->
                                                            CustomMessages.failable(
                                                                    sender,
                                                                    successKey,
                                                                    "Error.failedMapIconUpdate",
                                                                    err2,
                                                                    Placeholder.unparsed(
                                                                            "type", args[0]),
                                                                    Placeholder.unparsed(
                                                                            "name", args[2])));
                                });

                return true;
            }
            default -> {
                CustomMessages.sendMessage(sender, "Error.invalidArgs");
                return false;
            }
        }

        completableFuture.whenComplete(
                (result, err) -> {
                    CustomMessages.failable(
                            sender,
                            successKey,
                            "Error.failedMapIconUpdate",
                            err,
                            Placeholder.unparsed("type", args[0]),
                            Placeholder.unparsed("name", args[1]));

                    PluginHookManager.get()
                            .getPluginHooks(MapPlugin.class, true)
                            .forEach(
                                    mapPlugin ->
                                            mapPlugin.updateIcon(
                                                    args[1],
                                                    MapAssetManager.IconType.valueOf(
                                                            args[0].toUpperCase()),
                                                    null));
                });
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String s,
            @NotNull final String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(
                    args[0], Arrays.asList("warp", "spawn", "home"), new ArrayList<>());
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "warp" -> StringUtil.copyPartialMatches(
                        args[1], AdvancedTeleportAPI.getWarps().keySet(), new ArrayList<>());
                case "spawn" -> StringUtil.copyPartialMatches(
                        args[1], AdvancedTeleportAPI.getSpawns().keySet(), new ArrayList<>());
                case "home" -> StringUtil.copyPartialMatches(
                        args[1],
                        Bukkit.getOnlinePlayers().stream()
                                .filter(
                                        player ->
                                                !(sender instanceof Player)
                                                        || ((Player) sender).canSee(player))
                                .map(Player::getName)
                                .toList(),
                        new ArrayList<>());
                default -> new ArrayList<>();
            };
        }

        if (args.length == 3) {
            return switch (args[0].toLowerCase()) {
                case "warp", "spawn" -> StringUtil.copyPartialMatches(
                        args[2], completions, new ArrayList<>());
                case "home" -> Optional.ofNullable(ATPlayer.getPlayer(args[1]))
                        .map(player -> player.getHomes().keySet())
                        .map(
                                homes ->
                                        StringUtil.copyPartialMatches(
                                                args[2], homes, new ArrayList<>()))
                        .map(List.class::cast) // Cast so we can return the empty list.
                        .orElse(List.of());
                default -> new ArrayList<>();
            };
        }

        if (args.length == 4 && args[3].equalsIgnoreCase("home")) {
            return StringUtil.copyPartialMatches(args[3], completions, new ArrayList<>());
        }

        return List.of();
    }
}
