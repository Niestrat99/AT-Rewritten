package io.github.niestrat99.advancedteleport.managers;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class MovementManager implements Listener {

    private static final HashMap<UUID, ImprovedRunnable> movement = new HashMap<>();

    @EventHandler
    public void onMovement(PlayerMoveEvent event) {

        boolean cancelled = willCancelTimer(event);

        UUID uuid = event.getPlayer().getUniqueId();
        if (cancelled && movement.containsKey(uuid)) {
            ImprovedRunnable timer = movement.get(uuid);
            timer.cancel();
            CustomMessages.sendMessage(event.getPlayer(), "Teleport.eventMovement");
            ParticleManager.removeParticles(event.getPlayer(), timer.command);
            movement.remove(uuid);
        }
    }

    private static boolean willCancelTimer(PlayerMoveEvent event) {
        boolean cancelOnRotate = MainConfig.get().CANCEL_WARM_UP_ON_ROTATION.get() && !event.getPlayer().hasPermission("at.admin.bypass.rotation");
        boolean cancelOnMove = MainConfig.get().CANCEL_WARM_UP_ON_MOVEMENT.get() && !event.getPlayer().hasPermission("at.admin.bypass.movement");

        boolean cancelled = false;

        // If we have to perform position checks, compare blocks
        if (cancelOnMove) {
            Location locTo = event.getTo();
            Location locFrom = event.getFrom();
            if (MainConfig.get().CHECK_EXACT_COORDINATES.get()) {
                if (locTo.getX() != locFrom.getX() // If the player moved
                        || locTo.getY() != locFrom.getY()
                        || locTo.getZ() != locFrom.getZ()) {
                    cancelled = true;
                }
            } else {
                if (locTo.getBlockX() != locFrom.getBlockX() // If the player moved
                        || locTo.getBlockY() != locFrom.getBlockY()
                        || locTo.getBlockZ() != locFrom.getBlockZ()) {
                    cancelled = true;
                }
            }
        }

        // If we have to perform rotation checks, compare
        if (cancelOnRotate && !cancelled) {
            Location locTo = event.getTo();
            Location locFrom = event.getFrom();

            if (locTo.getPitch() != locFrom.getPitch()
                || locTo.getYaw() != locFrom.getYaw()) {
                cancelled = true;
            }
        }

        return cancelled;
    }

    public static HashMap<UUID, ImprovedRunnable> getMovement() {
        return movement;
    }

    public static void createMovementTimer(
            Player teleportingPlayer,
            Location location,
            String command,
            String message,
            int warmUp,
            TagResolver... placeholders) {
        createMovementTimer(
                teleportingPlayer,
                location,
                command,
                message,
                warmUp,
                teleportingPlayer,
                placeholders);
    }

    public static void createMovementTimer(
            Player teleportingPlayer,
            Location location,
            String command,
            String message,
            int warmUp,
            Player payingPlayer,
            TagResolver... placeholders) {
        UUID uuid = teleportingPlayer.getUniqueId();

        // When this config is enabled the teleporting player will receive a blindness effect until
        // it gets teleported.
        if (MainConfig.get().BLINDNESS_ON_WARMUP.get()) {
            teleportingPlayer.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.BLINDNESS, warmUp * 20 + 20, 0, false, false));
        }

        // Apply the plugin particles.
        ParticleManager.applyParticles(teleportingPlayer, command);

        // Starts the movement checker.
        ImprovedRunnable movementtimer =
                new ImprovedRunnable(command) {
                    @Override
                    public void run() {

                        // If the player can't pay for the
                        if (!PaymentManager.getInstance()
                                .canPay(command, payingPlayer, location.getWorld())) return;
                        ParticleManager.onTeleport(teleportingPlayer, command);
                        ATPlayer.teleportWithOptions(
                                teleportingPlayer,
                                location,
                                PlayerTeleportEvent.TeleportCause.COMMAND);
                        movement.remove(uuid);
                        CustomMessages.sendMessage(teleportingPlayer, message, placeholders);
                        PaymentManager.getInstance()
                                .withdraw(command, payingPlayer, location.getWorld());
                        // If the cooldown is to be applied after only after a teleport takes place,
                        // apply it now
                        if (MainConfig.get()
                                .APPLY_COOLDOWN_AFTER
                                .get()
                                .equalsIgnoreCase("teleport")) {
                            CooldownManager.addToCooldown(
                                    command, payingPlayer, location.getWorld());
                        }
                    }
                };
        movement.put(uuid, movementtimer);
        movementtimer.runTaskLater(CoreClass.getInstance(), warmUp * 20L);
        if ((MainConfig.get().CANCEL_WARM_UP_ON_MOVEMENT.get() && !teleportingPlayer.hasPermission("at.admin.bypass.movement"))
                || (MainConfig.get().CANCEL_WARM_UP_ON_ROTATION.get() && !teleportingPlayer.hasPermission("at.admin.bypass.rotation"))) {
            CustomMessages.sendMessage(
                    teleportingPlayer,
                    "Teleport.eventBeforeTP",
                    Placeholder.unparsed("countdown", String.valueOf(warmUp)),
                    Placeholder.component("countdown-formatted", CustomMessages.toTime(warmUp)));
        } else {
            CustomMessages.sendMessage(
                    teleportingPlayer,
                    "Teleport.eventBeforeTPMovementAllowed",
                    Placeholder.unparsed("countdown", String.valueOf(warmUp)),
                    Placeholder.component("countdown-formatted", CustomMessages.toTime(warmUp)));
        }
    }

    public abstract static class ImprovedRunnable extends BukkitRunnable {

        private final String command;

        ImprovedRunnable(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }
}
