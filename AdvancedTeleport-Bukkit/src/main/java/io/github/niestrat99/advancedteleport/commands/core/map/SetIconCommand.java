package io.github.niestrat99.advancedteleport.commands.core.map;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.managers.MapAssetManager;
import io.github.niestrat99.advancedteleport.sql.MetadataSQLManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SetIconCommand implements SubATCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (args.length < 3) {
            return true;
        }
        CompletableFuture<Boolean> completableFuture;
        switch (args[0].toLowerCase()) {
            case "warp":
                completableFuture = MetadataSQLManager.get().addWarpMetadata(args[1], "map_icon", args[2]);
                break;
            case "spawn":
                completableFuture = MetadataSQLManager.get().addSpawnMetadata(args[1], "map_icon", args[2]);
                break;
            case "home":
                if (args.length < 4) {
                    return true;
                }
                CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(args[1]).getUniqueId(), CoreClass.async)
                        .thenAcceptAsync(uuid -> MetadataSQLManager.get().addHomeMetadata(args[2], uuid, "map_icon", args[3]));
                return true;
            default:
                return false;
        }

        completableFuture.thenAcceptAsync(result -> sender.sendMessage(String.valueOf(result)));
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("warp", "home", "spawn"), results);
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "warp" -> StringUtil.copyPartialMatches(args[1], AdvancedTeleportAPI.getWarps().keySet(), results);
                case "home" -> {
                    List<String> players = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!(sender instanceof Player) || ((Player) sender).canSee(player)) {
                            players.add(player.getName());
                        }
                    }
                    StringUtil.copyPartialMatches(args[1], players, results);
                }
                case "spawn" -> StringUtil.copyPartialMatches(args[1], Spawn.get().getSpawns(), results);
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "warp", "spawn" ->
                        StringUtil.copyPartialMatches(args[2], MapAssetManager.getImageNames(), results);
                case "home" -> {
                    ATPlayer player = ATPlayer.getPlayer(args[1]);
                    if (player != null) {
                        StringUtil.copyPartialMatches(args[2], player.getHomes().keySet(), results);
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[3].equalsIgnoreCase("home")) {
                StringUtil.copyPartialMatches(args[3], MapAssetManager.getImageNames(), results);
            }
        }
        return results;
    }
}
