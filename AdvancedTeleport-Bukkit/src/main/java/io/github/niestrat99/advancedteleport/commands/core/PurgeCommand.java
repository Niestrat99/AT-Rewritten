package io.github.niestrat99.advancedteleport.commands.core;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.commands.SubATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import io.github.niestrat99.advancedteleport.sql.WarpSQLManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PurgeCommand implements SubATCommand {

    // This command is going to purge warps and homes - the homes can be purged for the certain player or if the homes/warps are in a certain world.
    // example: /at purge <warps|homes> <world|player> <World name|Player name>

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Check for if the arguments are lower than 3 - if that's the case the command will stop
        if (args.length < 3) {
            CustomMessages.sendMessage(sender, "Error.tooFewArguments");
            return false;
        }

        // Check for if the arguments are not wrong, typo'd or missing - if that's the case the command will stop
        if (!args[0].equalsIgnoreCase("warps") && !args[0].equalsIgnoreCase("homes")) {
            CustomMessages.sendMessage(sender, "Error.invalidArgs");
            return false;
        }

        // If they've not specified whether it's a world or player being purged, stop them
        if (!args[1].equalsIgnoreCase("world") && !args[1].equalsIgnoreCase("player")) {
            CustomMessages.sendMessage(sender, "Error.invalidArgs");
            return false;
        }
        if (args[1].equalsIgnoreCase("world")) {
            if (Bukkit.getWorld(args[2]) == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchWorld");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "homes" ->
                        HomeSQLManager.get().purgeHomes(Bukkit.getWorld(args[2]).getName(), new SQLManager.SQLCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                CustomMessages.sendMessage(sender, "Info.purgeHomesWorld", "{world}", args[2]);
                            }

                            @Override
                            public void onFail() {
                                CustomMessages.sendMessage(sender, "Error.purgeHomesFail");
                            }
                        });
                case "warps" ->
                        WarpSQLManager.get().purgeWarps(Bukkit.getWorld(args[2]).getName(), new SQLManager.SQLCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                CustomMessages.sendMessage(sender, "Info.purgeWarpsWorld", "{world}", args[2]);
                            }

                            @Override
                            public void onFail() {
                                CustomMessages.sendMessage(sender, "Error.purgeWarpsFail");
                            }
                        });
            }

        } else if (args[1].equalsIgnoreCase("player")) {
            Bukkit.getScheduler().runTaskAsynchronously(CoreClass.getInstance(), () -> {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
                if (player.getName() == null) {
                    CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                    return;
                }
                switch (args[0].toLowerCase()) {
                    case "homes" ->
                            HomeSQLManager.get().purgeHomes(player.getUniqueId(), new SQLManager.SQLCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    CustomMessages.sendMessage(sender, "Info.purgeHomesCreator", "{player}", args[2]);
                                }

                                @Override
                                public void onFail() {
                                    CustomMessages.sendMessage(sender, "Error.purgeHomesFail");
                                }
                            });
                    case "warps" ->
                            WarpSQLManager.get().purgeWarps(player.getUniqueId(), new SQLManager.SQLCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    CustomMessages.sendMessage(sender, "Info.purgeWarpsCreator", "{player}", args[2]);
                                }

                                @Override
                                public void onFail() {
                                    CustomMessages.sendMessage(sender, "Error.purgeWarpsFail");
                                }
                            });
                }
            });
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("homes", "warps"), results);

        } else if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("player", "world"), results);

        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("player")) {
                List<String> players = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
                StringUtil.copyPartialMatches(args[2], players, results);

            } else if (args[1].equalsIgnoreCase("world")) {
                List<String> worlds = new ArrayList<>();
                Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
                StringUtil.copyPartialMatches(args[2], worlds, results);
            }
        }

        return results;
    }
}
