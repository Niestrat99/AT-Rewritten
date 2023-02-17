package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class HomesCommand extends AbstractHomeCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        if (args.length > 0 && sender.hasPermission("at.admin.homes")) {
            ATPlayer.getPlayerFuture(args[0]).thenAccept(player -> {
                if (player.getHomes() == null || player.getHomes().size() == 0) {
                    CustomMessages.sendMessage(sender, "Error.homesNotLoaded");
                    return;
                }
                getHomes(sender, player.getOfflinePlayer());
            });
            return true;
        }

        getHomes(sender, (Player) sender);
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.homes";
    }

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_HOMES.get();
    }

    private void getHomes(CommandSender sender, OfflinePlayer target) {
        ATPlayer atPlayer = ATPlayer.getPlayer(target);

        if (atPlayer instanceof ATFloodgatePlayer atFloodgatePlayer && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
            atFloodgatePlayer.sendHomeForm();
            return;
        }

        FancyMessage hList = new FancyMessage();

        String infoPath = "Info.homes";
        String extraArg = "";
        String noHomes = "Error.noHomes";
        if (sender != target) {
            infoPath = "Info.homesOther";
            extraArg = target.getName() + " ";
            noHomes = "Error.noHomesOtherPlayer";
        }

        hList.text(CustomMessages.getString(infoPath, "{player}", target.getName()));
        if (atPlayer.getHomes().size() > 0) {
            for (Home home : atPlayer.getHomes().values()) {
                if (atPlayer.canAccessHome(home) || sender.hasPermission("at.admin.homes")) {
                    hList.then(home.getName())
                            .command("/home " + extraArg + home.getName())
                            .tooltip(getTooltip(sender, home))
                            .then(", ");
                } else if (!MainConfig.get().HIDE_HOMES_IF_DENIED.get()) {
                    hList.then(home.getName())
                            .tooltip(getTooltip(sender, home))
                            .color(ChatColor.GRAY)
                            .style(ChatColor.ITALIC)
                            .then(", ");
                }

            }
            hList.text(""); //Removes trailing comma
        } else {
            hList.text(CustomMessages.getString(noHomes, "{player}", target.getName()));
        }

        Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
            hList.sendProposal(sender, 0);
            FancyMessage.send(sender);
        });
    }

    private List<String> getTooltip(CommandSender sender, Home home) {
        List<String> tooltip = new ArrayList<>(Collections.singletonList(CustomMessages.getStringRaw("Tooltip.homes")));
        if (sender.hasPermission("at.member.homes.location")) {
            tooltip.addAll(Arrays.asList(CustomMessages.getStringRaw("Tooltip.location").split("\n")));
        }
        List<String> homeTooltip = new ArrayList<>(tooltip);
        for (int i = 0; i < homeTooltip.size(); i++) {
            Location homeLoc = home.getLocation();

            homeTooltip.set(i, homeTooltip.get(i).replace("{home}", home.getName())
                    .replaceAll("\\{x}", String.valueOf(homeLoc.getX()))
                    .replaceAll("\\{y}", String.valueOf(homeLoc.getY()))
                    .replaceAll("\\{z}", String.valueOf(homeLoc.getZ()))
                    .replaceAll("\\{world}", homeLoc.getWorld().getName()));
        }
        return homeTooltip;
    }
}
