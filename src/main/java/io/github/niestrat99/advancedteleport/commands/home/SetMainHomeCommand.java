package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.AsyncATCommand;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.sql.SQLManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetMainHomeCommand extends AbstractHomeCommand implements AsyncATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (NewConfig.get().USE_HOMES.get()) {
            if (sender.hasPermission("at.member.setmainhome")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ATPlayer atPlayer = ATPlayer.getPlayer(player);
                    if (args.length > 0) {
                        String homeName = args[0];
                        if (atPlayer.hasHome(homeName)) {
                            Home home = atPlayer.getHome(homeName);
                            if (atPlayer.canAccessHome(home)) {
                                atPlayer.setMainHome(homeName, SQLManager.SQLCallback.getDefaultCallback(
                                        sender, "Info.setMainHome", "Error.setMainHomeFail", "{home}", homeName));

                            } else {
                                sender.sendMessage("No access lol");
                            }
                        } else {
                            if (atPlayer.canSetMoreHomes()) {

                                atPlayer.addHome(homeName, player.getLocation(), callback ->
                                        atPlayer.setMainHome(homeName, SQLManager.SQLCallback.getDefaultCallback(
                                                sender, "Info.setAndMadeMainHome", "Error.setMainHomeFail", "{home}", homeName)));
                            }
                        }
                    } else {
                        sender.sendMessage("needs a home name loser");
                    }
                }
            }
        }
        return true;
    }
}
