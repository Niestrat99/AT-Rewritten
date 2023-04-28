package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.sql.HomeSQLManager;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoveHomeCommand extends AbstractHomeCommand implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        if (!NewConfig.get().USE_HOMES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.member.movehome")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }

        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        if (args.length == 0) {
            CustomMessages.sendMessage(sender, "Error.noHomeInput");
            return true;
        }
        if (sender.hasPermission("at.admin.movehome")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            // We'll just assume that the admin command overrides the homes limit.
            if (args.length > 1) {
                ATPlayer.getPlayerFuture(args[0]).thenAccept(atTarget -> {
                    if (!atTarget.getHomes().containsKey(args[1])) {
                        CustomMessages.sendMessage(sender, "Error.noSuchHome");
                        return;
                    }
                    HomeSQLManager.get().moveHome(player.getLocation(), target.getUniqueId(), args[1],
                            SQLManager.SQLCallback.getDefaultCallback(sender, "Info.movedHomeOther", "Error.moveHomeFail",
                                    "{player}", args[0], "{home}", args[1]));

                });
                return true;
            }
        }

        Home home = atPlayer.getHome(args[0]);
        if (home == null) {
            CustomMessages.sendMessage(sender, "Error.noSuchHome");
            return true;
        }
        atPlayer.moveHome(args[0], player.getLocation(),
                SQLManager.SQLCallback.getDefaultCallback(
                        sender, "Info.movedHome", "Error.moveHomeFail", "{home}", args[0]));

        return true;
    }
}
