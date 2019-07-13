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
                    if (Config.isUsingEXPPayment("tpahere")) {
                        if (request.getRequester().getLevel()>Config.getEXPTeleportPrice("tpahere")){
                            int currentLevel = request.getRequester().getLevel();
                            request.getRequester().setLevel(currentLevel - Config.getEXPTeleportPrice("tpahere"));
                            request.getRequester().sendMessage(ChatColor.GREEN + "You have paid " + ChatColor.AQUA + Config.getTeleportPrice("tpahere") + ChatColor.GREEN + " EXP Levels for your teleportation Request. You now have " + ChatColor.AQUA + request.getRequester().getLevel() + ChatColor.GREEN + " EXP Levels!");
                        }
                    }
                    if  (Main.getVault() != null && Config.isUsingVault("tpahere")) {
                        if (Main.getVault().getBalance(request.getRequester())>Config.getTeleportPrice("tpahere")){
                            EconomyResponse payment = Main.getVault().withdrawPlayer(request.getRequester() , Config.getTeleportPrice("tpahere"));
                            if (payment.transactionSuccess()){
                                request.getRequester().sendMessage(ChatColor.GREEN + "You have paid $" + ChatColor.AQUA + Config.getTeleportPrice("tpahere") + ChatColor.GREEN + " for your teleportation Request. You now have $" + ChatColor.AQUA + Main.getVault().getBalance(request.getRequester()) + ChatColor.GREEN + "!");
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
                    if (Config.isUsingEXPPayment("tpa")) {
                        if (request.getRequester().getLevel()>Config.getEXPTeleportPrice("tpa")){
                            int currentLevel = request.getRequester().getLevel();
                            request.getRequester().setLevel(currentLevel - Config.getEXPTeleportPrice("tpa"));
                            request.getRequester().sendMessage(ChatColor.GREEN + "You have paid " + ChatColor.AQUA + Config.getEXPTeleportPrice("tpa") + ChatColor.GREEN + " EXP Levels for your teleportation Request. You now have " + ChatColor.AQUA + request.getRequester().getLevel() + ChatColor.GREEN + " EXP Levels!");
                        }
                    }
                    if  (Main.getVault() != null && Config.isUsingVault("tpa")) {
                        if (Main.getVault().getBalance(request.getRequester())>=Config.getTeleportPrice("tpa")){
                            EconomyResponse payment = Main.getVault().withdrawPlayer(request.getRequester() , Config.getTeleportPrice("tpa"));
                            if (payment.transactionSuccess()){
                                request.getRequester().sendMessage(ChatColor.GREEN + "You have paid $" + ChatColor.AQUA + Config.getTeleportPrice("tpa") + ChatColor.GREEN + " for your teleportation Request. You now have $" + ChatColor.AQUA + Main.getVault().getBalance(request.getRequester()) + ChatColor.GREEN + "!");
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
