package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import io.github.niestrat99.advancedteleport.utilities.TPRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;


public class Tpa implements ATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }
        Player player = (Player) sender;
        if (!NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get()) {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
            return true;
        }
        if (sender.hasPermission("at.member.tpa")) {
            UUID playerUuid = player.getUniqueId();
            int cooldown = CooldownManager.secondsLeftOnCooldown("tpa", player);
            if (cooldown > 0) {
                CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
                return true;
            }
            if (MovementManager.getMovement().containsKey(playerUuid)) {
                CustomMessages.sendMessage(player, "Error.onCountdown");
                return true;
            }
            if (args.length == 0) {
                ATPlayer atPlayer = ATPlayer.getPlayer(player);
                if (atPlayer instanceof ATFloodgatePlayer) {
                    ((ATFloodgatePlayer) atPlayer).sendTPAForm();
                } else {
                    CustomMessages.sendMessage(sender, "Error.noPlayerInput");
                }
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            String result = ConditionChecker.canTeleport(player, target, "tpa");
            if (!result.isEmpty()) {
                CustomMessages.sendMessage(player, result, "{player}", args[0], "{world}", target == null ? "<No Such World>" : target.getWorld().getName());
                return true;
            }
            if (PaymentManager.getInstance().canPay("tpa", player)) {
                int requestLifetime = NewConfig.get().REQUEST_LIFETIME.get();

                CustomMessages.sendMessage(sender, "Info.requestSent",
                        "{player}", target.getName(),
                        "{lifetime}", String.valueOf(requestLifetime));

                CoreClass.playSound("tpa", "sent", player);

                ATPlayer targetPlayer = ATPlayer.getPlayer(target);

                if (targetPlayer instanceof ATFloodgatePlayer) {
                    ((ATFloodgatePlayer) targetPlayer).sendRequestFormTPA(player);
                } else {
                    CustomMessages.sendMessage(target, "Info.tpaRequestReceived",
                            "{player}", sender.getName(),
                            "{lifetime}", String.valueOf(requestLifetime));
                }

                CoreClass.playSound("tpa", "received", target);

                BukkitRunnable run = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (NewConfig.get().NOTIFY_ON_EXPIRE.get()) {
                            CustomMessages.sendMessage(sender, "Error.requestExpired", "{player}", target.getName());
                        }
                        TPRequest.removeRequest(TPRequest.getRequestByReqAndResponder(target, player));
                    }
                };
                run.runTaskLater(CoreClass.getInstance(), requestLifetime * 20L); // 60 seconds
                TPRequest request = new TPRequest(player, target, run, TPRequest.TeleportType.TPA); // Creates a new teleport request.
                TPRequest.addRequest(request);
                // If the cooldown is to be applied after request, apply it now
                if (NewConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("request")) {
                    CooldownManager.addToCooldown("tpa", player);
                }
                return true;
            }

        }
        return true;
    }
}
