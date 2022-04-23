package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.players.TeleportRequestEvent;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TpaHere extends TeleportATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerUuid = player.getUniqueId();
            int cooldown = CooldownManager.secondsLeftOnCooldown("tpahere", player);
            if (cooldown > 0) {
                CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
                return true;
            }
            if (MovementManager.getMovement().containsKey(playerUuid)) {
                CustomMessages.sendMessage(player, "Error.onCountdown");
                return true;
            }
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                String result = ConditionChecker.canTeleport(player, target, "tpahere");
                if (result.isEmpty()) {
                    assert target != null;
                    if (PaymentManager.getInstance().canPay("tpahere", player)) {

                        TeleportRequestEvent event = new TeleportRequestEvent(target, player, TeleportRequestType.TPAHERE);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            // Cannot send request
                            return true;
                        }

                        int requestLifetime = NewConfig.get().REQUEST_LIFETIME.get();
                        CustomMessages.sendMessage(sender, "Info.requestSent",
                                "{player}", target.getName(), "{lifetime}", String.valueOf(requestLifetime));

                        CoreClass.playSound("tpahere", "sent", player);

                        CustomMessages.sendMessage(target, "Info.tpaRequestHere",
                                "{player}", sender.getName(), "{lifetime}", String.valueOf(requestLifetime));

                        CoreClass.playSound("tpahere", "received", target);

                        BukkitRunnable run = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (NewConfig.get().NOTIFY_ON_EXPIRE.get()) {
                                    CustomMessages.sendMessage(sender, "Error.requestExpired", "{player}",
                                            target.getName());
                                }
                                TeleportRequest.removeRequest(TeleportRequest.getRequestByReqAndResponder(target,
                                        player));
                            }
                        };
                        run.runTaskLater(CoreClass.getInstance(), requestLifetime * 20L); // 60 seconds
                        TeleportRequest request = new TeleportRequest(player, target, run,
                                TeleportRequestType.TPAHERE); // Creates a new teleport request.
                        TeleportRequest.addRequest(request);
                        // If the cooldown is to be applied after request or accept (they are the same in the case of
                        // /spawn), apply it now
                        if (NewConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("request")) {
                            CooldownManager.addToCooldown("tpahere", player);
                        }
                        return true;
                    }
                } else {
                    CustomMessages.sendMessage(player, result, "{player}", args[0], "{world}", target == null ? "<No " +
                            "Such World>" : target.getWorld().getName());
                    return true;
                }
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                return true;
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "at.member.here";
    }
}
