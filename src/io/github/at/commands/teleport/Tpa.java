package io.github.at.commands.teleport;

import io.github.at.config.Config;
import io.github.at.config.TpBlock;
import io.github.at.events.CooldownManager;
import io.github.at.main.Main;
import io.github.at.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Tpa implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Config.featTP()) {
                if (sender.hasPermission("tbh.tp.member.tpa")) {
                    if (CooldownManager.getCooldown().containsKey(player)) {
                        sender.sendMessage(ChatColor.RED + "This command has a cooldown of " + Config.commandCooldown() + " seconds each use - Please wait!");
                        return false;
                    }
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase(player.getName())){
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + "You cannot send a teleport request to yourself!");
                            return false;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " Either the player is currently offline or doesn't exist.");
                            return false;
                        } else {
                            if (TpOff.getTpOff().contains(target)) {
                                sender.sendMessage(ChatColor.YELLOW + target.getName() + ChatColor.RED + " has disabled to receive teleport requests!");
                                return false;
                            }
                            if (TpBlock.getBlockedPlayers(target).contains(player)) {
                                sender.sendMessage(ChatColor.RED + "You can not teleport to " + ChatColor.YELLOW + target.getName() + ChatColor.RED + "!");
                                return false;
                            }
                            if (TPRequest.getRequestByReqAndResponder(target, player) != null) {
                                sender.sendMessage(ChatColor.RED + "You already have sent a teleport request to " + ChatColor.YELLOW + target.getName() + ChatColor.RED + "!");
                                return false;
                            }
                            if (Config.EXPPayment()){
                                if (player.getLevel()<Config.EXPTeleportPrice()){
                                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You do not have enough EXP Levels to send a teleport request to someone else!");
                                    player.sendMessage(ChatColor.RED + "You need at least " + ChatColor.YELLOW + Config.EXPTeleportPrice() + ChatColor.RED + " EXP Levels!");
                                    return false;
                                }
                            }
                            if (Main.getVault() != null && Config.useVault()) {
                                if (Main.getVault().getBalance(player)<Config.teleportPrice()){
                                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + "You do not have enough money to send a teleport request to someone else!");
                                    player.sendMessage(ChatColor.RED + "You need at least $" + ChatColor.YELLOW + Config.teleportPrice() + ChatColor.RED + "!");
                                    return false;
                                }
                            }
                            sender.sendMessage(ChatColor.GREEN + "Teleport request send to " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + "!");
                            sender.sendMessage(ChatColor.GREEN + "They've got " + ChatColor.AQUA + Config.requestLifetime() + ChatColor.GREEN + " seconds to respond!");
                            sender.sendMessage(ChatColor.GREEN + "To cancel the request use " + ChatColor.AQUA + "/tpcancel " + ChatColor.GREEN + "to cancel it.");
                            target.sendMessage(ChatColor.GREEN + "The Player " + ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + " wants to teleport to you!");
                            target.sendMessage(ChatColor.GREEN + "If you want to accept it use " + ChatColor.AQUA + "/tpayes " + ChatColor.GREEN + ", if not use" + ChatColor.AQUA + "/tpano" + ChatColor.GREEN + ".");
                            target.sendMessage(ChatColor.GREEN + "You've got " + ChatColor.AQUA + Config.requestLifetime() + ChatColor.GREEN + " seconds to respond to the request!");
                            BukkitRunnable run = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    sender.sendMessage(ChatColor.GREEN + "Your teleport request to " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + " has expired!");
                                    TPRequest.removeRequest(TPRequest.getRequestByReqAndResponder(target, player));
                                }
                            };
                            run.runTaskLater(Main.getInstance(), Config.requestLifetime()*20); // 60 seconds
                            TPRequest request = new TPRequest(player, target, run, TPRequest.TeleportType.TPA_NORMAL); // Creates a new teleport request.
                            TPRequest.addRequest(request);
                            BukkitRunnable cooldowntimer = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    CooldownManager.getCooldown().remove(player);
                                }
                            };
                            CooldownManager.getCooldown().put(player, cooldowntimer);
                            cooldowntimer.runTaskLater(Main.getInstance(), Config.commandCooldown()*20); // 20 ticks = 1 second
                            return false;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR:" + ChatColor.RED + " You must include a player name!");
                        return false;
                    }
                }
                return false;
            }
        } return false;
    }
}
