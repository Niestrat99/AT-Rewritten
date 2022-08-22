package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        // If the sender isn't a player
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
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
            } else if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
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
            CustomMessages.sendMessage(sender, "Error.homeAlreadySet", "{home}", homeName);
            return;
        }

        atPlayer.addHome(homeName, sender.getLocation(), sender).thenAccept(result -> CustomMessages.sendMessage(sender, result ?
                        (sender.getUniqueId() == player ? "Info.setHome" : "Info.setHomeOther") : "Error.setHomeFail",
                "{home}", homeName, "{player}", playerName));

    }


    @Override
    public boolean getRequiredFeature() {
        return NewConfig.get().USE_HOMES.get();
    }

    @Override
    public String getPermission() {
        return "at.member.sethome";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                      @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
