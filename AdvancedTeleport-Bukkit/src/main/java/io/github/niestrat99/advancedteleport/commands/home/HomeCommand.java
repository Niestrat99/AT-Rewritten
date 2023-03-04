package io.github.niestrat99.advancedteleport.commands.home;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.TimedATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public final class HomeCommand extends AbstractHomeCommand implements TimedATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        final var player = (Player) sender;
        final var atPlayer = ATPlayer.getPlayer(player);

        final var homes = atPlayer.getHomes();

        // If more than one argument has been specified and the player is an admin...
        if (args.length > 1 && sender.hasPermission("at.admin.home")) {
            ATPlayer.getPlayerFuture(args[0]).thenAccept(target -> {

                target.getHomesAsync().thenAcceptAsync(homesOther -> {

                    // If the home has been set with the specific name, use that
                    Home home = homesOther.get(args[1]);
                    if (home != null) {
                        PaperLib.teleportAsync(player, home.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        CustomMessages.sendMessage(sender, "Teleport.teleportingToHomeOther", "{player}", args[0], "{home}", args[1]);
                        return;
                    }

                    // If we're using a bed, try getting the bed spawn
                    if (args[1].equalsIgnoreCase("bed") && MainConfig.get().ADD_BED_TO_HOMES.get()) {
                        home = target.getBedSpawn();
                        if (home != null) {
                            PaperLib.teleportAsync(player, home.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                            CustomMessages.sendMessage(sender, "Teleport.teleportingToHomeOther", "{player}", args[0], "{home}", args[1]);
                            return;
                        }
                    }

                    // If we're requesting a list, just throw it
                    if (args[1].equalsIgnoreCase("list")) {
                        Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> Bukkit.dispatchCommand(sender, "advancedteleport:homes " + args[0]));
                        return;
                    }

                    // Tell the player there is no such home
                    CustomMessages.sendMessage(sender, "Error.noSuchHome");
                }, CoreClass.sync);
            });
        }

        // If there's no arguments specified...
        if (args.length == 0) {

            // Try getting the main home - if it exists, teleport there
            Home home = atPlayer.getMainHome();
            if (home != null) {
                teleport(player, home);
                return true;
            }

            // If the player only has one home, then get the first one
            if (homes.size() == 1) {
                home = homes.values().iterator().next();
                if (atPlayer.canAccessHome(home)) {
                    teleport(player, home);
                } else {
                    CustomMessages.sendMessage(sender, "Error.noAccessHome", "{home}", home.getName());
                }
                return true;
            }

            // If the player has a bed to teleport to, then assume they want to go there
            if (MainConfig.get().ADD_BED_TO_HOMES.get()) {
                home = atPlayer.getBedSpawn();
                if (home != null) {
                    teleport(player, home);
                    return true;
                }
            }

            // If there's nowhere to go, let them know
            if (homes.isEmpty()) {
                CustomMessages.sendMessage(sender, "Error.noHomes");
            } else {
                CustomMessages.sendMessage(sender, "Error.noHomeInput");
            }
            return true;
        }

        Home home = atPlayer.getHome(args[0]);
        if (home != null && atPlayer.canAccessHome(home)) {
            teleport(player, home);
            return true;
        }

        if (args[0].equalsIgnoreCase("bed") && MainConfig.get().ADD_BED_TO_HOMES.get()) {
            home = atPlayer.getBedSpawn();
            if (home == null) {
                CustomMessages.sendMessage(player, "Error.noBedHome");
                return true;
            }

            teleport(player, home);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> Bukkit.dispatchCommand(sender,
                    "advancedteleport:homes " + args[0]));
            return true;
        }

        CustomMessages.sendMessage(sender, (home == null ? "Error.noSuchHome" : "Error.noAccessHome"), "{home}", args[0]);
        return true;
    }

    public static void teleport(
        @NotNull final Player player,
        @NotNull final Home home
    ) {
        ATTeleportEvent event = new ATTeleportEvent(
                player,
                home.getLocation(),
                player.getLocation(),
                home.getName(),
                ATTeleportEvent.TeleportType.HOME
        );
        Bukkit.getPluginManager().callEvent(event);
        ATPlayer.getPlayer(player).teleport(event, "home", "Teleport.teleportingToHome");
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.home";
    }

    @Override
    public String getSection() {
        return "home";
    }
}
