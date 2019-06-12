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

    private void acceptRequest(TpRequest Request) {
        Player player = Request.getResponder();
        Request.getRequester().sendMessage(ChatColor.YELLOW + "" + player.getName() + ChatColor.GREEN + " has accepted your teleport Request!");
        player.sendMessage(ChatColor.GREEN + "You've accepted the teleport Request!");
        if (Request.getType() == TpRequest.TeleportType.TPA_HERE) {
            BukkitRunnable movementtimer = new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(Request.getRequester());
                    MovementManager.getMovement().remove(player);
                    player.sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                    if (Config.EXPPayment()) {
                        if (Request.getRequester().getLevel()>Config.EXPTeleportPrice()){
                            int currentLevel = Request.getRequester().getLevel();
                            Request.getRequester().setLevel(currentLevel - Config.EXPTeleportPrice());
                            Request.getRequester().sendMessage(ChatColor.GREEN + "You have paid " + ChatColor.AQUA + Config.EXPTeleportPrice() + ChatColor.GREEN + " EXP Levels for your teleportation Request. You now have " + ChatColor.AQUA + Request.getRequester().getLevel() + ChatColor.GREEN + " EXP Levels!");
                        }
                    }
                    if  (Main.getVault() != null && Config.useVault()) {
                        if (Main.getVault().getBalance(Request.getRequester())>Config.teleportPrice()){
                            EconomyResponse payment = Main.getVault().withdrawPlayer(Request.getRequester() , Config.teleportPrice());
                            if (payment.transactionSuccess()){
                                Request.getRequester().sendMessage(ChatColor.GREEN + "You have paid $" + ChatColor.AQUA + Config.teleportPrice() + ChatColor.GREEN + " for your teleportation Request. You now have $" + ChatColor.AQUA + Main.getVault().getBalance(Request.getRequester()) + ChatColor.GREEN + "!");
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
                    Request.getRequester().teleport(player);
                    MovementManager.getMovement().remove(Request.getRequester());
                    Request.getRequester().sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                    if (Config.EXPPayment()) {
                        if (Request.getRequester().getLevel()>Config.EXPTeleportPrice()){
                            int currentLevel = Request.getRequester().getLevel();
                            Request.getRequester().setLevel(currentLevel - Config.EXPTeleportPrice());
                            Request.getRequester().sendMessage(ChatColor.GREEN + "You have paid " + ChatColor.AQUA + Config.EXPTeleportPrice() + ChatColor.GREEN + " EXP Levels for your teleportation Request. You now have " + ChatColor.AQUA + Request.getRequester().getLevel() + ChatColor.GREEN + " EXP Levels!");
                        }
                    }
                    if  (Main.getVault() != null && Config.useVault()) {
                        if (Main.getVault().getBalance(Request.getRequester())>=Config.teleportPrice()){
                            EconomyResponse payment = Main.getVault().withdrawPlayer(Request.getRequester() , Config.teleportPrice());
                            if (payment.transactionSuccess()){
                                Request.getRequester().sendMessage(ChatColor.GREEN + "You have paid $" + ChatColor.AQUA + Config.teleportPrice() + ChatColor.GREEN + " for your teleportation Request. You now have $" + ChatColor.AQUA + Main.getVault().getBalance(Request.getRequester()) + ChatColor.GREEN + "!");
                            } else {
                                Request.getRequester().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR: " + ChatColor.RED + payment.errorMessage);
                            }
                        }


                    }
                }
            };
            MovementManager.getMovement().put(Request.getRequester(), movementtimer);
            movementtimer.runTaskLater(Main.getInstance(), Config.teleportTimer()*20);
            Request.getRequester().sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.teleportTimer())));
        }
        Request.destroy();
    }

}
