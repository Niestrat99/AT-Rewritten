package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.events.CooldownManager;
import io.github.niestrat99.advancedteleport.events.MovementManager;
import io.github.niestrat99.advancedteleport.utilities.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("spawn")) {
            if (sender.hasPermission("at.member.spawn")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int cooldown = CooldownManager.secondsLeftOnCooldown("spawn", player);
                    if (cooldown > 0) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
                        return true;
                    }
                    if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                        sender.sendMessage(CustomMessages.getString("Error.onCountdown"));
                        return true;
                    }
                    spawn(player);
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    public static void spawn(Player player) {
        Location spawn;
        if (Spawn.getSpawnFile() != null) {
            spawn = Spawn.getSpawnFile();
        } else {
            spawn = player.getWorld().getSpawnLocation();
        }
        ATTeleportEvent event = new ATTeleportEvent(player, spawn, player.getLocation(), "spawn", ATTeleportEvent.TeleportType.SPAWN);
        if (!event.isCancelled()) {
            if (PaymentManager.canPay("spawn", player)) {
                CooldownManager.addToCooldown("spawn", player);
                if (Config.getTeleportTimer("spawn") > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    BukkitRunnable movementtimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            PaymentManager.withdraw("spawn", player);
                            PaperLib.teleportAsync(player, spawn);
                            player.sendMessage(CustomMessages.getString("Teleport.teleportingToSpawn"));
                            MovementManager.getMovement().remove(player.getUniqueId());
                        }
                    };
                    MovementManager.getMovement().put(player.getUniqueId(), movementtimer);
                    movementtimer.runTaskLater(CoreClass.getInstance(), Config.getTeleportTimer("spawn") * 20);
                    player.sendMessage(CustomMessages.getEventBeforeTPMessage().replaceAll("\\{countdown}", String.valueOf(Config.getTeleportTimer("spawn"))));

                } else {
                    PaymentManager.withdraw("spawn", player);
                    PaperLib.teleportAsync(player, spawn);
                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToSpawn"));
                }
            }
        }
    }
}
