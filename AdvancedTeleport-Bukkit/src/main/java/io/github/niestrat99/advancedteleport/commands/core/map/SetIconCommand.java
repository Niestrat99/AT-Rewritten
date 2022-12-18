package io.github.niestrat99.advancedteleport.commands.core.map;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.managers.MapAssetManager;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SetIconCommand extends SubATCommand {
    @Override
    @Contract
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (args.length < 3) {
            return true;
        }

        CompletableFuture<Boolean> completableFuture;
        switch (args[0].toLowerCase()) {
            case "warp" -> completableFuture = MetadataSQLManager.get().addWarpMetadata(args[1], "map_icon", args[2]);
            case "spawn" -> completableFuture = MetadataSQLManager.get().addSpawnMetadata(args[1], "map_icon", args[2]);
            case "home" -> {
                if (args.length < 4) return true;

                CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(args[1]).getUniqueId(), CoreClass.async)
                        .thenAcceptAsync(uuid -> MetadataSQLManager.get().addHomeMetadata(args[2], uuid, "map_icon", args[3]));

                return true;
            }
            default -> {
                return false;
            }
        }

        completableFuture.thenAcceptAsync(result -> sender.sendMessage(String.valueOf(result)));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("warp", "spawn", "home"), new ArrayList<>());
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "warp" -> StringUtil.copyPartialMatches(args[1], AdvancedTeleportAPI.getWarps().keySet(), new ArrayList<>());
                case "spawn" -> StringUtil.copyPartialMatches(args[1], Spawn.get().getSpawns(), new ArrayList<>());
                case "home" -> StringUtil.copyPartialMatches(
                    args[1],
                    Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !(sender instanceof Player) || ((Player) sender).canSee(player))
                        .map(Player::getName)
                        .toList(),
                    new ArrayList<>()
                );
            };
        }

        if (args.length == 3) {
            return switch (args[0].toLowerCase()) {
                case "warp", "spawn" -> StringUtil.copyPartialMatches(args[2], MapAssetManager.getImageNames(), new ArrayList<>());
                case "home" -> Optional.ofNullable(ATPlayer.getPlayer(args[1]))
                    .map(player -> player.getHomes().keySet())
                    .map(homes -> StringUtil.copyPartialMatches(args[2], homes, new ArrayList<>()))
                    .map(List.class::cast) // Cast so we can return the empty list.
                    .orElse(List.of());
                };
            }

        if (args.length == 4 && args[3].equalsIgnoreCase("home")) {
            return StringUtil.copyPartialMatches(args[3], MapAssetManager.getImageNames(), new ArrayList<>());
        }

        return List.of();
    }
}
