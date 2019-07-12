package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.events.CooldownManager;
import io.github.at.events.MovementManager;
import io.github.at.main.Main;
import io.github.at.utilities.RandomCoords;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static io.github.at.utilities.RandomCoords.generateCoords;

public class Tpr implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (Config.isFeatureEnabled("randomTP")) {
                if (sender.hasPermission("at.member.tpr")) {
                    World world = player.getWorld();
                    if (args.length > 0) {
                        if (sender.hasPermission("at.member.tpr.other")) {
                            World otherWorld = Bukkit.getWorld(args[0]);
                            if (otherWorld != null) {
                                world = otherWorld;
                            } else {
                                sender.sendMessage(ChatColor.RED + "The world " + args[0] + " doesn't exist!");
                                return false;
                            }
                        }

                    }
                    if (CooldownManager.getCooldown().containsKey(player)) {
                        sender.sendMessage(ChatColor.RED + "This command has a cooldown of " + Config.commandCooldown() + " seconds each use - Please wait!");
                        return false;
                    }
                    if (Config.EXPPayment()){
                        if (player.getLevel()<Config.EXPTPRCost()){
                            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You do not have enough EXP Levels to use /tpr!");
                            player.sendMessage(ChatColor.RED + "You need at least " + ChatColor.YELLOW + Config.EXPTPRCost() + ChatColor.RED + " EXP Levels!");
                            return false;
                        }
                    }
                    if (Main.getVault() != null && Config.useVault()) {
                        if (Main.getVault().getBalance(player)<Config.vaultTPRCost()){
                            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You do not have enough money to use /tpr!");
                            player.sendMessage(ChatColor.RED + "You need at least $" + ChatColor.YELLOW + Config.vaultTPRCost() + ChatColor.RED + "!");
                            return false;
                        }
                    }
                    Location location = generateCoords(player, world);
                    player.sendMessage(ChatColor.GREEN + "Searching for a location...");
                    boolean validLocation = false;
                    while (!validLocation) {
                        while (location.getBlock().getType() == Material.AIR) {
                            location.subtract(0, 1, 0);
                        }
                        boolean b = true;
                        for (String Material: Config.avoidBlocks()) {
                            if (location.getBlock().getType().name().equalsIgnoreCase(Material)){
                                location = RandomCoords.generateCoords(player, world);
                                b = false;
                                break;
                            }
                        }
                        if (b) {
                            location.add(0 , 1 , 0);
                            validLocation = true;
                        }
                    }
                    Chunk chunk = player.getWorld().getChunkAt(location);
                    chunk.load(true);
                    BukkitRunnable cooldowntimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            CooldownManager.getCooldown().remove(player);
                        }
                    };
                    CooldownManager.getCooldown().put(player, cooldowntimer);
                    cooldowntimer.runTaskLater(Main.getInstance(), Config.commandCooldown() * 20); // 20 ticks = 1 second
                    Location loc = location;
                    BukkitRunnable movementtimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(loc);
                            MovementManager.getMovement().remove(player);
                            sender.sendMessage(ChatColor.GREEN + "You've been teleported to a random place!");
                            if (Config.EXPPayment()) {
                                if (player.getLevel()>Config.EXPTeleportPrice()){
                                    int currentLevel = player.getLevel();
                                    player.setLevel(currentLevel - Config.EXPTeleportPrice());
                                    player.sendMessage(ChatColor.GREEN + "You have paid " + ChatColor.AQUA + Config.EXPTPRCost() + ChatColor.GREEN + " EXP Levels for your teleportation request. You now have " + ChatColor.AQUA + player.getLevel() + ChatColor.GREEN + " EXP Levels!");
                                }
                            }
                            if  (Main.getVault() != null && Config.useVault()) {
                                if (Main.getVault().getBalance(player)>Config.teleportPrice()){
                                    EconomyResponse payment = Main.getVault().withdrawPlayer(player , Config.vaultTPRCost());
                                    if (payment.transactionSuccess()){
                                        player.sendMessage(ChatColor.GREEN + "You have paid $" + ChatColor.AQUA + Config.vaultTPRCost() + ChatColor.GREEN + " for your teleportation request. You now have $" + ChatColor.AQUA + Main.getVault().getBalance(player) + ChatColor.GREEN + "!");
                                    }
                                }
                            }
                        }
                    };
                    MovementManager.getMovement().put(player, movementtimer);
                    movementtimer.runTaskLater(Main.getInstance(), Config.teleportTimer() * 20);
                    player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.teleportTimer())));
                    return false;
                }
            }
        }
        return false;
    }
}
