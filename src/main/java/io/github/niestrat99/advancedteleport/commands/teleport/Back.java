package io.github.niestrat99.advancedteleport.commands.teleport;

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
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (NewConfig.getInstance().USE_BASIC_TELEPORT_FEATURES.get()) {
            if (sender.hasPermission("at.member.back")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int cooldown = CooldownManager.secondsLeftOnCooldown("back", player);
                    if (cooldown > 0) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
                        return true;
                    }
                    Location loc = TeleportTrackingManager.getLastLocation(player.getUniqueId());
                    if (loc == null) {
                        loc = LastLocations.getLocation(player);
                        if (loc == null) {
                            sender.sendMessage(CustomMessages.getString("Error.noLocation"));
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
                    ATTeleportEvent event = new ATTeleportEvent(player, loc, player.getLocation(), "back", ATTeleportEvent.TeleportType.BACK);
                    if (!event.isCancelled()) {
                        if (PaymentManager.getInstance().canPay("back", player)) {
                            int warmUp = NewConfig.getInstance().WARM_UPS.BACK.get();
                            if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                                MovementManager.createMovementTimer(player, loc, "back", "Teleport.teleportingToLastLoc", warmUp);
                            } else {
                                PaymentManager.getInstance().withdraw("back", player);
                                PaperLib.teleportAsync(player, loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                               
                                player.sendMessage(CustomMessages.getString("Teleport.teleportingToLastLoc"));
                            }
                            // If the cooldown is to be applied after request or accept (they are the same in the case of /back), apply it now
                            String cooldownConfig = NewConfig.getInstance().APPLY_COOLDOWN_AFTER.get();
                            if(cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
                                CooldownManager.addToCooldown("back", player);
                            }
                        }
                    }

                    if (!DistanceLimiter.canTeleport(player.getLocation(), loc, "back") && !player.hasPermission("at.admin.bypass.distance-limit")) {
                        player.sendMessage(CustomMessages.getString("Error.tooFarAway"));
                        return true;
                    }


                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            } else {
                sender.sendMessage(CustomMessages.getString("Error.noPermission"));
                return true;
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
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
