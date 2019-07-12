package io.github.at.commands.spawn;

import io.github.at.config.Config;
import io.github.at.config.Spawn;
import io.github.at.events.MovementManager;
import io.github.at.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (Config.isFeatureEnabled("spawn")) {
            if (commandSender.hasPermission("tbh.tp.member.spawn")){
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    BukkitRunnable movementtimer = new BukkitRunnable() {

                        @Override
                        public void run() {
                            if (Spawn.getSpawn() != null) {
                                player.teleport(Spawn.getSpawn());
                                commandSender.sendMessage(ChatColor.GREEN + "Teleporting you to the spawn.");
                                MovementManager.getMovement().remove(player);
                            } else {
                                player.teleport(player.getWorld().getSpawnLocation());
                                commandSender.sendMessage(ChatColor.GREEN + "Teleporting you to the spawn.");
                                MovementManager.getMovement().remove(player);
                            }

                        }
                    };
                    MovementManager.getMovement().put(player, movementtimer);
                    movementtimer.runTaskLater(Main.getInstance(), Config.teleportTimer() * 20);
                    commandSender.sendMessage(ChatColor.GREEN + "Teleporting in " + ChatColor.AQUA + Config.teleportTimer() + " seconds" + ChatColor.GREEN + ", please don't move!");
                    return false;

                }
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "The feature " + ChatColor.GOLD + "Spawn " + ChatColor.RED + "is disabled!");
            return false;
        }
        return false;
    }
}
