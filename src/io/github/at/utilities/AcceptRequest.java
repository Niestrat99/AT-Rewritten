package io.github.at.utilities;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.events.MovementManager;
import io.github.at.main.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AcceptRequest {

    public static void acceptRequest(TPRequest request) {
        Player player = request.getResponder();
        request.getRequester().sendMessage(ChatColor.YELLOW + "" + player.getName() + ChatColor.GREEN + " has accepted your teleport Request!");
        player.sendMessage(ChatColor.GREEN + "You've accepted the teleport Request!");
        if (request.getType() == TPRequest.TeleportType.TPA_HERE) {
            BukkitRunnable movementtimer = new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(request.getRequester());
                    MovementManager.getMovement().remove(player);
                    player.sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                    if (Config.EXPPayment()) {
                        if (request.getRequester().getLevel()>Config.EXPTeleportPrice()){
                            int currentLevel = request.getRequester().getLevel();
                            request.getRequester().setLevel(currentLevel - Config.EXPTeleportPrice());
                            request.getRequester().sendMessage(ChatColor.GREEN + "You have paid " + ChatColor.AQUA + Config.EXPTeleportPrice() + ChatColor.GREEN + " EXP Levels for your teleportation Request. You now have " + ChatColor.AQUA + request.getRequester().getLevel() + ChatColor.GREEN + " EXP Levels!");
                        }
                    }
                    if  (Main.getVault() != null && Config.useVault()) {
                        if (Main.getVault().getBalance(request.getRequester())>Config.teleportPrice()){
                            EconomyResponse payment = Main.getVault().withdrawPlayer(request.getRequester() , Config.teleportPrice());
                            if (payment.transactionSuccess()){
                                request.getRequester().sendMessage(ChatColor.GREEN + "You have paid $" + ChatColor.AQUA + Config.teleportPrice() + ChatColor.GREEN + " for your teleportation Request. You now have $" + ChatColor.AQUA + Main.getVault().getBalance(request.getRequester()) + ChatColor.GREEN + "!");
                            }
                        }
                    }

                }
            };
            MovementManager.getMovement().put(player, movementtimer);
            movementtimer.runTaskLater(Main.getInstance(), Config.teleportTimer()*20);
            player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.teleportTimer())));
        } else {
            BukkitRunnable movementtimer = new BukkitRunnable() {
                @Override
                public void run() {
                    request.getRequester().teleport(player);
                    MovementManager.getMovement().remove(request.getRequester());
                    request.getRequester().sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                    if (Config.EXPPayment()) {
                        if (request.getRequester().getLevel()>Config.EXPTeleportPrice()){
                            int currentLevel = request.getRequester().getLevel();
                            request.getRequester().setLevel(currentLevel - Config.EXPTeleportPrice());
                            request.getRequester().sendMessage(ChatColor.GREEN + "You have paid " + ChatColor.AQUA + Config.EXPTeleportPrice() + ChatColor.GREEN + " EXP Levels for your teleportation Request. You now have " + ChatColor.AQUA + request.getRequester().getLevel() + ChatColor.GREEN + " EXP Levels!");
                        }
                    }
                    if  (Main.getVault() != null && Config.useVault()) {
                        if (Main.getVault().getBalance(request.getRequester())>=Config.teleportPrice()){
                            EconomyResponse payment = Main.getVault().withdrawPlayer(request.getRequester() , Config.teleportPrice());
                            if (payment.transactionSuccess()){
                                request.getRequester().sendMessage(ChatColor.GREEN + "You have paid $" + ChatColor.AQUA + Config.teleportPrice() + ChatColor.GREEN + " for your teleportation Request. You now have $" + ChatColor.AQUA + Main.getVault().getBalance(request.getRequester()) + ChatColor.GREEN + "!");
                            } else {
                                request.getRequester().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + payment.errorMessage);
                            }
                        }


                    }
                }
            };
            MovementManager.getMovement().put(request.getRequester(), movementtimer);
            movementtimer.runTaskLater(Main.getInstance(), Config.teleportTimer()*20);
            request.getRequester().sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.teleportTimer())));
        }
        request.destroy();
    }

}
