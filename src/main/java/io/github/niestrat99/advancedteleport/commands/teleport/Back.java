package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.LastLocations;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.managers.TeleportTrackingManager;
import io.github.niestrat99.advancedteleport.utilities.DistanceLimiter;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Back implements ATCommand {

    private final List<String> airMaterials = new ArrayList<>(Arrays.asList("AIR", "WATER", "CAVE_AIR"));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String s, @NotNull String[] strings) {
        if (NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.member.back")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int cooldown = CooldownManager.secondsLeftOnCooldown("back", player);
                    if (cooldown > 0) {
                        CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
                        return true;
                    }
                    Location loc = TeleportTrackingManager.getLastLocation(player.getUniqueId());
                    if (loc == null) {
                        loc = LastLocations.getLocation(player);
                        if (loc == null) {
                            CustomMessages.sendMessage(sender, "Error.noLocation");
                            return true;
                        }
                    }
                    double originalY = loc.getY();
                    while (!airMaterials.contains(loc.getBlock().getType().name())) {
                        // If we go beyond max height, stop and reset the Y value
                        if (loc.getY() > loc.getWorld().getMaxHeight()) {
                            loc.setY(originalY);
                            break;
                        }
                        loc.add(0.0, 1.0, 0.0);
                    }

                    if (!DistanceLimiter.canTeleport(player.getLocation(), loc, "back") && !player.hasPermission("at.admin.bypass.distance-limit")) {
                        CustomMessages.sendMessage(player, "Error.tooFarAway");
                        return true;
                    }

                    ATTeleportEvent event = new ATTeleportEvent(player, loc, player.getLocation(), "back", ATTeleportEvent.TeleportType.BACK);
                    ATPlayer.getPlayer(player).teleport(event, "back", "Teleport.teleportingToLastLoc", NewConfig.get().WARM_UPS.BACK.get());

                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noPermission");
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
