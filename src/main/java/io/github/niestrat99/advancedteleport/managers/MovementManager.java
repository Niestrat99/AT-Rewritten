package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class MovementManager implements Listener {

    private static HashMap<UUID, BukkitRunnable> movement = new HashMap<>();

    @EventHandler
    public void onMovement(PlayerMoveEvent event) {
        boolean cancelOnRotate = NewConfig.getInstance().CANCEL_WARM_UP_ON_ROTATION.get();
        boolean cancelOnMove = NewConfig.getInstance().CANCEL_WARM_UP_ON_MOVEMENT.get();
        if (!cancelOnRotate) {
            Location locTo = event.getTo();
            Location locFrom = event.getFrom();
            if (locTo.getBlockX() == locFrom.getBlockX() // If the player rotated instead of moved
                    && locTo.getBlockY() == locFrom.getBlockY()
                    && locTo.getBlockZ() == locTo.getBlockZ()) {
                return;
            }
        }
        UUID uuid = event.getPlayer().getUniqueId();
        if ((cancelOnRotate || cancelOnMove) && movement.containsKey(uuid)) {
            BukkitRunnable timer = movement.get(uuid);
            timer.cancel();
            event.getPlayer().sendMessage(CustomMessages.getString("Teleport.eventMovement"));
            movement.remove(uuid);
        }
    }

    public static HashMap<UUID, BukkitRunnable> getMovement() {
        return movement;
    }

    public static void createMovementTimer(Player teleportingPlayer, Location location, String command, String message, int warmUp, String... placeholders) {
        createMovementTimer(teleportingPlayer, location, command, message, warmUp, teleportingPlayer, placeholders);
    }

    public static void createMovementTimer(Player teleportingPlayer, Location location, String command, String message, int warmUp, Player payingPlayer, String... placeholders) {
        UUID uuid = teleportingPlayer.getUniqueId();
        String actualMessage = CustomMessages.getString(message);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i > placeholders.length - 2) break;
            actualMessage = actualMessage.replace(placeholders[i], placeholders[i + 1]);
        }
        final String finalMessage = actualMessage;
        BukkitRunnable movementtimer = new BukkitRunnable() {
            @Override
            public void run() {
                PaperLib.teleportAsync(teleportingPlayer, location, PlayerTeleportEvent.TeleportCause.COMMAND);
                movement.remove(uuid);

                teleportingPlayer.sendMessage(finalMessage);
                PaymentManager.getInstance().withdraw(command, payingPlayer);
                // If the cooldown is to be applied after only after a teleport takes place, apply it now
                if(NewConfig.getInstance().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("teleport")) {
                    CooldownManager.addToCooldown(command, payingPlayer);
                }
            }
        };
        movement.put(uuid, movementtimer);
        movementtimer.runTaskLater(CoreClass.getInstance(), warmUp * 20);
        teleportingPlayer.sendMessage(CustomMessages.getEventBeforeTPMessage().replaceAll("\\{countdown}" , String.valueOf(warmUp)));

    }
}
