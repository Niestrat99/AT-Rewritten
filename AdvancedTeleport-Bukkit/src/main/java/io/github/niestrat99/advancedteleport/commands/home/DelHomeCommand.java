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

public class DelHomeCommand extends AbstractHomeCommand implements AsyncATCommand {

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

        Player player = (Player) sender;
        if (!sender.hasPermission("at.member.delhome")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
        if (args.length > 0) {
            if (sender.hasPermission("at.admin.delhome")) {
                if (args.length > 1) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                    delHome(target, player, args[1]);
                    return true;
                }
            }
            delHome(player, args[0]);
        } else {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer) {
                ((ATFloodgatePlayer) atPlayer).sendDeleteHomeForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }
        }
        return true;
    }

    private void delHome(OfflinePlayer player, Player sender, String name) {
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (!atPlayer.hasHome(name)) {
            CustomMessages.sendMessage(sender, "Error.noSuchHome");
            return;
        }
        atPlayer.removeHome(name, SQLManager.SQLCallback.getDefaultCallback(sender,
                sender.getUniqueId() == player.getUniqueId() ? "Info.deletedHome" : "Info.deletedHomeOther",
                "Error.setHomeFail", "{home}", name, "{player}", player.getName()));
    }

    private void delHome(Player player, String name) {
        delHome(player, player, name);
    }
}
