package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.commands.TimedATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.utilities.DistanceLimiter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Back extends TeleportATCommand implements TimedATCommand {

    private final List<String> airMaterials = new ArrayList<>(Arrays.asList("AIR", "WATER", "CAVE_AIR"));

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        // Make sure the sender has permission and the associated feature is enabled
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;
        // Initialise player variables
        if (args.length > 0 && sender.hasPermission("at.admin.back")) {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                return true;
            }
        }

        // Get the ATPlayer object
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        // Get the player's previous location
        Location loc = atPlayer.getPreviousLocation();

        // If it's null, we have nowhere to go
        if (loc == null) {
            CustomMessages.sendMessage(sender, "Error.noLocation");
            return true;
        }

        // Get the original previous location
        double originalY = loc.getY();
        double originalX = loc.getX();
        double originalZ = loc.getZ();
        int radius = MainConfig.get().BACK_SEARCH_RADIUS.get();
        ArrayList<Location> possiblelocs = new ArrayList<>();

        /*
         * I note, that this loop may has up to radius^3 Calculations. But it fixes the issue, that users
         * complain, that they are ported at a different location.
         * Furthermore the default search radius of 5 equals 125 calculations which is acceptable
         */
        Location t = new Location(loc.getWorld(), originalX, originalY, originalZ);
        for (int dx = -radius; dx <= radius; dx++) {
            t.setX(originalX - dx);
            for (int dz = -radius; dz <= radius; dz++) {
                t.setZ(originalZ - dz);
                for (int dy = -radius; dy <= radius; dy++) {
                    t.setY(originalY - dy);
                    if (!t.getBlock().getType().name().equals("LAVA")
                        && !airMaterials.contains(t.getBlock().getType().name())
                        && airMaterials.contains(t.clone().add(0.0, 1.0, 0.0).getBlock().getType().name())
                        && airMaterials.contains(t.clone().add(0.0, 2.0, 0.0).getBlock().getType().name())) {
                        possiblelocs.add(new Location(
                            loc.getWorld(),
                            loc.getBlockX() - dx + 0.5,
                            loc.getBlockY() - dy + 1.0,
                            loc.getBlockZ() - dz + 0.5
                        ));
                    }
                }
            }
        }

        // If there's more than one location to go through, remove them one by one - the shortest distance should be kept.
        while (possiblelocs.size() > 1) {
            if (loc.distanceSquared(possiblelocs.get(1)) > loc.distanceSquared(possiblelocs.get(0))) possiblelocs.remove(1);
            else possiblelocs.remove(0);
        }

        // If there's only one location to retrieve, get that location
        if (possiblelocs.size() == 1) loc = possiblelocs.get(0);

        // Check for bad blocks
        int lavablocks = 0;
        while (!airMaterials.contains(loc.getBlock().getType().name()) && possiblelocs.isEmpty()) {

            // If we go beyond max height, stop and reset the Y value
            if (loc.getBlock().getType().name().equalsIgnoreCase("Lava")) ++lavablocks;
            if (loc.getY() > loc.getWorld().getMaxHeight() || lavablocks > 5) {
                loc.setY(originalY);
                break;
            }
            loc.add(0.0, 1.0, 0.0);
        }
        // The total count of operations in a worstcase is 128

        // If the distance limiter is in effect and it's too far, stop there
        if (!DistanceLimiter.canTeleport(player.getLocation(), loc, "back", ATPlayer.getPlayer(player))
            && !player.hasPermission("at.admin.bypass.distance-limit")) {
            CustomMessages.sendMessage(player, "Error.tooFarAway");
            return true;
        }

        // Call the event
        ATTeleportEvent event = new ATTeleportEvent(player, loc, player.getLocation(), "back", ATTeleportEvent.TeleportType.BACK);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        // Teleport the target player
        if (sender != player) {
            CustomMessages.sendMessage(player, "Teleport.teleportingToLastLoc");
            player.teleport(loc);
        } else {
            atPlayer.teleport(event, "back", "Teleport.teleportingToLastLoc");
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.back";
    }

    @Override
    public @NotNull String getSection() {
        return "back";
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        return null;
    }
}
