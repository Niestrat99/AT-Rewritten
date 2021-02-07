package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetHome implements AsyncATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (NewConfig.getInstance().USE_HOMES.get()) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (sender.hasPermission("at.member.sethome")) {
                    if (args.length>0) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                        if (sender.hasPermission("at.admin.sethome")) {
                            // We'll just assume that the admin command overrides the homes limit.
                            if (args.length > 1) {
                                setHome(player, target.getUniqueId(), args[1], args[0]);
                                return true;
                            }
                        }

                        // I don't really want to run this method twice if a player has a lot of permissions, so store it as an int
                        int limit = getHomesLimit(player);

                        // If the number of homes a player has is smaller than or equal to the homes limit, or they have a bypass permission
                        if (atPlayer.getHomes().size() < limit
                                || player.hasPermission("at.admin.sethome.bypass")
                                || limit == -1) {
                            setHome(player, args[0]);

                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.reachedHomeLimit"));
                        }
                    } else {
                        int limit = getHomesLimit(player);
                        if (atPlayer.getHomes().size() == 0 && (limit > 0 || limit == -1)) {
                            setHome(player, "home");
                        } else {
                            sender.sendMessage(CustomMessages.getString("Error.noHomeInput"));
                        }
                    }
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    private void setHome(Player sender, String name) {
        setHome(sender, sender.getUniqueId(), name, sender.getName());
    }

    // Separated this into a separate method so that the code is easier to read.
    // Player player - the player which is having the home set.
    // String name - the name of the home.
    private void setHome(Player sender, UUID player, String homeName, String playerName) {
        OfflinePlayer settingPlayer = Bukkit.getOfflinePlayer(player);

        ATPlayer atPlayer = ATPlayer.getPlayer(settingPlayer);

        if (atPlayer.getHome(homeName) != null) {
            sender.sendMessage(CustomMessages.getString("Error.homeAlreadySet").replace("{home}", homeName));
            return;
        }

        atPlayer.addHome(homeName, sender.getLocation(), data -> {
            if (sender.getUniqueId() == player) {
                sender.sendMessage(CustomMessages.getString("Info.setHome").replace("{home}", homeName));
            } else {
                sender.sendMessage(CustomMessages.getString("Info.setHomeOther").replace("{home}", homeName).replaceAll("\\{player}", playerName));
            }
        });
    }

    // Used to get the permission for how many homes a player can have.
    // If there is no permission, then it's assumed that the number of homes they can have is limitless (-1).
    // E.g.: at.member.homes.5
    // at.member.homes.40
    // at.member.homes.100000
    private int getHomesLimit(Player player) {
        int maxHomes = NewConfig.getInstance().DEFAULT_HOMES_LIMIT.get();
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().startsWith("at.member.homes.")) {
                if (permission.getValue()) {
                    String perm = permission.getPermission();
                    String ending = perm.substring(perm.lastIndexOf(".") + 1);
                    if (ending.equalsIgnoreCase("unlimited")) return -1;
                    if (!ending.matches("^[0-9]+$")) continue;
                    int homes = Integer.parseInt(ending);
                    if (maxHomes < homes) {
                        maxHomes = homes;
                    }
                }
            }
        }
        return maxHomes;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
