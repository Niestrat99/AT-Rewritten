package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetHomeCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_HOMES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.member.sethome")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (args.length > 0) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (sender.hasPermission("at.admin.sethome") && player != target) {
                // We'll just assume that the admin command overrides the homes limit.
                if (args.length > 1) {
                    setHome(player, target.getUniqueId(), args[1], args[0]);
                    return true;
                }
            }

            if (atPlayer.canSetMoreHomes()) {
                setHome(player, args[0]);
            } else {
                CustomMessages.sendMessage(sender, "Error.reachedHomeLimit");
            }
        } else {
            int limit = atPlayer.getHomesLimit();
            if (atPlayer.getHomes().size() == 0 && (limit > 0 || limit == -1)) {
                setHome(player, "home");
            } else if (atPlayer instanceof ATFloodgatePlayer) {
                ((ATFloodgatePlayer) atPlayer).sendSetHomeForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }
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
            if (NewConfig.get().OVERWRITE_SETHOME.get()) {
                if (!sender.hasPermission("at.member.movehome")) { CustomMessages.sendMessage(sender, "Error.noPermission"); return; }
                // If the player has permission to do this then hey, congratulations!
                atPlayer.moveHome(homeName, sender.getLocation(), SQLManager.SQLCallback.getDefaultCallback(sender,
                        sender.getUniqueId() == player ? "Info.movedHome" : "Info.movedHomeOther",
                        "Error.moveHomeFail", "{home}", homeName, "{player}", playerName));
            } else {
                CustomMessages.sendMessage(sender, "Error.homeAlreadySet", "{home}", homeName);
            }
            return;

        }

        atPlayer.addHome(homeName, sender.getLocation(), SQLManager.SQLCallback.getDefaultCallback(sender,
                sender.getUniqueId() == player ? "Info.setHome" : "Info.setHomeOther",
                "Error.setHomeFail", "{home}", homeName, "{player}", playerName));

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
