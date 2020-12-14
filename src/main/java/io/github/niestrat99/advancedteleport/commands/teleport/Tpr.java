package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.events.CooldownManager;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.utilities.RandomTPAlgorithms;
import io.papermc.lib.PaperLib;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Tpr implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (NewConfig.getInstance().USE_RANDOMTP.get()) {
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
        if (NewConfig.getInstance().WHITELIST_WORLD.get()) {
            List<String> allowedWorlds = NewConfig.getInstance().ALLOWED_WORLDS.get();
            if (!allowedWorlds.contains(world.getName())) {
                if (!player.hasPermission("at.admin.rtp.bypass-world")) {
                    if (allowedWorlds.isEmpty()) {
                        player.sendMessage(CustomMessages.getString("Error.cantTPToWorld"));
                        return true;
                    } else {
                        for (String worldName : allowedWorlds) {
                            world = Bukkit.getWorld(worldName);
                            if (world != null) break;
                        }
                        if (world == null) {
                            player.sendMessage(CustomMessages.getString("Error.cantTPToWorld"));
                            return true;
                        }
                    }
                }
            }
        }
        if (!PaymentManager.getInstance().canPay("tpr", player)) return false;
        player.sendMessage(CustomMessages.getString("Info.searching"));
        RandomTPAlgorithms.getAlgorithms().get("binary").fire(player, location -> Bukkit.getScheduler().runTask(CoreClass.getInstance(), () -> {
            ATTeleportEvent event = new ATTeleportEvent(player, location, player.getLocation(), "", ATTeleportEvent.TeleportType.TPR);
            if (!event.isCancelled()) {
                CooldownManager.addToCooldown("tpr", player);
                int warmUp = NewConfig.getInstance().WARM_UPS.TPR.get();
                if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    BukkitRunnable movementtimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            PaperLib.teleportAsync(player, location);
                            MovementManager.getMovement().remove(player.getUniqueId());
                            player.sendMessage(CustomMessages.getString("Teleport.teleportingToRandomPlace"));
                            PaymentManager.getInstance().withdraw("tpr", player);
                        }
                    };
                    MovementManager.getMovement().put(player.getUniqueId(), movementtimer);
                    movementtimer.runTaskLater(CoreClass.getInstance(), warmUp * 20);
                    player.sendMessage(CustomMessages.getEventBeforeTPMessage().replaceAll("\\{countdown}" , String.valueOf(warmUp)));
                } else {
                    PaperLib.teleportAsync(player, location);
                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToRandomPlace"));
                    PaymentManager.getInstance().withdraw("tpr", player);
                }
            }
        }));
        return true;
    }
}
