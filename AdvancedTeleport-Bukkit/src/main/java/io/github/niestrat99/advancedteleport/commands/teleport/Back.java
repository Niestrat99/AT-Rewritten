package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
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

public class Back implements ATCommand {

    private final List<String> airMaterials = new ArrayList<>(Arrays.asList("AIR", "WATER", "CAVE_AIR", "STATIONARY_WATER"));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (!sender.hasPermission("at.member.back")) {
            CustomMessages.sendMessage(sender, "Error.noPermission");
            return true;
        }
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        Player player = (Player) sender;
        if (args.length > 0 && sender.hasPermission("at.admin.back")) {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                CustomMessages.sendMessage(sender, "Error.noSuchPlayer");
                return true;
            }
        }

        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        int cooldown = CooldownManager.secondsLeftOnCooldown("back", player);
        if (cooldown > 0) {
            CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
            return true;
        }
        Location loc = atPlayer.getPreviousLocation();
        if (loc == null) {
            CustomMessages.sendMessage(sender, "Error.noLocation");
            return true;
        }
        double originalY = loc.getY();
        double originalX = loc.getX();
        double originalZ = loc.getZ();
        int radius = NewConfig.get().BACK_SEARCH_RADIUS.get();
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
                        possiblelocs.add(new Location(loc.getWorld(),
                                loc.getBlockX() - dx + 0.5,
                                loc.getBlockY() - dy + 1,
                                loc.getBlockZ() - dz + 0.5));
                    }
                }
            }
        }
        while (possiblelocs.size() > 1) {
            if (loc.distance(possiblelocs.get(1)) > loc.distance(possiblelocs.get(0))) possiblelocs.remove(1);
            else possiblelocs.remove(0);
        }
        if (possiblelocs.size() == 1) loc = possiblelocs.get(0);
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

        if (!DistanceLimiter.canTeleport(player.getLocation(), loc, "back") && !player.hasPermission("at.admin.bypass.distance-limit")) {
            CustomMessages.sendMessage(player, "Error.tooFarAway");
            return true;
        }

        ATTeleportEvent event = new ATTeleportEvent(player, loc, player.getLocation(), "back", ATTeleportEvent.TeleportType.BACK);
        if (sender != player) {
            CustomMessages.sendMessage(player, "Teleport.teleportingToLastLoc");
            player.teleport(loc);
        } else {
            atPlayer.teleport(event, "back", "Teleport.teleportingToLastLoc");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
