package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetMainHomeCommand extends AbstractHomeCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

        Player player = (Player) sender;
        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        if (args.length == 0) {
            if (atPlayer instanceof ATFloodgatePlayer && NewConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendSetMainHomeForm();
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }
            return true;
        }
        if (args.length > 1 && sender.hasPermission("at.admin.setmainhome")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target != player) {
                ATPlayer atTarget = ATPlayer.getPlayer(target);
                String homeName = args[1];

                if (atTarget.hasHome(homeName)) {
                    atPlayer.setMainHome(homeName).thenAcceptAsync(result ->
                            CustomMessages.sendMessage(sender, result ? "Info.setMainHomeOther" : "Error" +
                                            ".setMainHomeFail",
                                    "{home}", homeName, "{player}", args[0]));
                } else {
                    if (atPlayer.canSetMoreHomes()) {
                        atPlayer.addHome(homeName, player.getLocation(), player).thenAcceptAsync(result ->
                                        atPlayer.setMainHome(homeName).thenAcceptAsync(result2 ->
                                                        CustomMessages.sendMessage(sender, result2 ? "Info" +
                                                                ".setAndMadeMainHome" :
                                                                "Error.setMainHomeFail", "{home}", homeName),
                                                CoreClass.async),
                                CoreClass.async);
                    }
                }
                return true;
            }

        }

        String homeName = args[0];
        if (atPlayer.hasHome(homeName)) {
            Home home = atPlayer.getHome(homeName);
            if (atPlayer.canAccessHome(home)) {
                atPlayer.setMainHome(homeName).thenAcceptAsync(result ->
                        CustomMessages.sendMessage(sender, result ? "Info.setMainHome" : "Error" +
                                        ".setMainHomeFail",
                                "{home}", homeName), CoreClass.async);
            } else {
                CustomMessages.sendMessage(sender, "Error.noAccessHome", "{home}", home.getName());
            }
        } else {
            if (atPlayer.canSetMoreHomes()) {
                atPlayer.addHome(homeName, player.getLocation(), player).thenAcceptAsync(result ->
                                atPlayer.setMainHome(homeName).thenAcceptAsync(result2 ->
                                                CustomMessages.sendMessage(sender, result2 ? "Info" +
                                                        ".setAndMadeMainHome" :
                                                        "Error.setMainHomeFail", "{home}", homeName),
                                        CoreClass.async),
                        CoreClass.async);
            } else {
                CustomMessages.sendMessage(sender, "Error.reachedHomeLimit");
            }
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.member.setmainhome";
    }
}
