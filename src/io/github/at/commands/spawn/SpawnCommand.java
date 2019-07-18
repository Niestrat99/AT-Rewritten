package io.github.at.commands.spawn;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.config.Spawn;
import io.github.at.events.MovementManager;
import io.github.at.main.Main;
import io.github.at.utilities.PaymentManager;
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
                    if (PaymentManager.canPay("spawn", player)) {
                        if (Config.getTeleportTimer("spawn") > 0) {
                            BukkitRunnable movementtimer = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (Spawn.getSpawn() != null) {
                                        player.teleport(Spawn.getSpawn());
                                    } else {
                                        player.teleport(player.getWorld().getSpawnLocation());
                                    }
                                    sender.sendMessage(CustomMessages.getString("Teleport.teleportingToSpawn"));
                                    MovementManager.getMovement().remove(player);
                                }
                            };
                            MovementManager.getMovement().put(player, movementtimer);
                            movementtimer.runTaskLater(Main.getInstance(), Config.getTeleportTimer("spawn") * 20);
                            sender.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}", String.valueOf(Config.getTeleportTimer("spawn"))));
                            return false;

                        } else {
                            PaymentManager.withdraw("spawn", player);
                            if (Spawn.getSpawn() != null) {
                                player.teleport(Spawn.getSpawn());
                            } else {
                                player.teleport(player.getWorld().getSpawnLocation());
                            }
                            sender.sendMessage(CustomMessages.getString("Teleport.teleportingToSpawn"));
                        }
                    }
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
            return false;
        }
        return false;
    }
}
