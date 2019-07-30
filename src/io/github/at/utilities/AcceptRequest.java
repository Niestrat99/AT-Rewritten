package io.github.at.utilities;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.events.MovementManager;
import io.github.at.events.TeleportTrackingManager;
import io.github.at.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AcceptRequest {

    public static void acceptRequest(TPRequest request) {
        Player player = request.getResponder();
        request.getRequester().sendMessage(CustomMessages.getString("Info.requestAcceptedResponder").replaceAll("\\{player}", player.getName()));
        player.sendMessage(CustomMessages.getString("Info.requestAccepted"));
        // Check again
        if (PaymentManager.canPay(request.getType().name().toLowerCase().replaceAll("_", ""), request.getRequester())) {
            if (request.getType() == TPRequest.TeleportType.TPA_HERE) {
                if (Config.getTeleportTimer("tpahere") > 0) {
                    BukkitRunnable movementtimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                            player.teleport(request.getRequester());
                            MovementManager.getMovement().remove(player);
                            player.sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                            PaymentManager.withdraw("tpahere", request.getRequester());

                        }
                    };
                    MovementManager.getMovement().put(player, movementtimer);
                    movementtimer.runTaskLater(Main.getInstance(), Config.getTeleportTimer("tpahere")*20);
                    player.sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.getTeleportTimer("tpahere"))));
                } else {
                    TeleportTrackingManager.getLastLocations().put(player, player.getLocation());
                    player.teleport(request.getRequester());
                    player.sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                    PaymentManager.withdraw("tpahere", request.getRequester());
                }
            } else {
                if (Config.getTeleportTimer("tpa") > 0) {
                    BukkitRunnable movementtimer = new BukkitRunnable() {
                        @Override
                        public void run() {
                            TeleportTrackingManager.getLastLocations().put(request.getRequester(), request.getRequester().getLocation());
                            request.getRequester().teleport(player);
                            MovementManager.getMovement().remove(request.getRequester());
                            request.getRequester().sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                            PaymentManager.withdraw("tpa", request.getRequester());
                        }
                    };
                    MovementManager.getMovement().put(request.getRequester(), movementtimer);
                    movementtimer.runTaskLater(Main.getInstance(), Config.getTeleportTimer("tpa")*20);
                    request.getRequester().sendMessage(CustomMessages.getString("Teleport.eventBeforeTP").replaceAll("\\{countdown}" , String.valueOf(Config.getTeleportTimer("tpa"))));

                } else {
                    TeleportTrackingManager.getLastLocations().put(request.getRequester(), request.getRequester().getLocation());
                    request.getRequester().teleport(player);
                    request.getRequester().sendMessage(CustomMessages.getString("Teleport.eventTeleport"));
                    PaymentManager.withdraw("tpa", request.getRequester());
                }
            }
        }

        request.destroy();
    }

}
