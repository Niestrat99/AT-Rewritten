package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.events.CooldownManager;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.utilities.DistanceLimiter;
import io.github.niestrat99.advancedteleport.utilities.PaymentManager;
import io.github.niestrat99.advancedteleport.utilities.RandomCoords;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Tpr implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (Config.isFeatureEnabled("randomTP")) {
                if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                    player.sendMessage(CustomMessages.getString("Error.onCountdown"));
                    return true;
                }
                if (sender.hasPermission("at.member.tpr")) {
                    World world = player.getWorld();
                    if (args.length > 0) {
                        if (sender.hasPermission("at.member.tpr.other")) {
                            World otherWorld = Bukkit.getWorld(args[0]);
                            if (otherWorld != null) {
                                world = otherWorld;
                            } else {
                                sender.sendMessage(CustomMessages.getString("Error.noSuchWorld"));
                                return true;
                            }
                        }
                    }
                    return randomTeleport(player, world);
                }

            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
        }
        return true;
    }

    public static boolean randomTeleport(Player player, World world) {
        int cooldown = CooldownManager.secondsLeftOnCooldown("tpr", player);
        if (cooldown > 0) {
            player.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
            return true;
        }
        if (Config.getBlacklistedTPRWorlds().contains(world.getName()) && !player.hasPermission("at.admin.rtp.bypass-world")) {
            player.sendMessage(CustomMessages.getString("Error.cantTPToWorld"));
            return true;
        }
        if (!PaymentManager.canPay("tpr", player)) return false;
        player.sendMessage(CustomMessages.getString("Info.searching"));
        RandomTPAlgorithms.getAlgorithms().get("binary").fire(player, location -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
            ATTeleportEvent event = new ATTeleportEvent(player, location, player.getLocation(), "", ATTeleportEvent.TeleportType.TPR);
            if (!event.isCancelled()) {
                CooldownManager.addToCooldown("tpr", player);
                if (Config.getTeleportTimer("tpr") > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    BukkitRunnable movementtimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            PaperLib.teleportAsync(player, location);
                            MovementManager.getMovement().remove(player.getUniqueId());
                            player.sendMessage(CustomMessages.getString("Teleport.teleportingToRandomPlace"));
                            PaymentManager.withdraw("tpr", player);
                        }
                    };
                    MovementManager.getMovement().put(player.getUniqueId(), movementtimer);
                    movementtimer.runTaskLater(CoreClass.getInstance(), Config.getTeleportTimer("tpr") * 20);
                    player.sendMessage(CustomMessages.getEventBeforeTPMessage().replaceAll("\\{countdown}" , String.valueOf(Config.getTeleportTimer("tpr"))));
                } else {
                    PaperLib.teleportAsync(player, location);
                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToRandomPlace"));
                    PaymentManager.withdraw("tpr", player);
                }
            }
        }));
        return true;
    }
}
