package io.github.niestrat99.advancedteleport.commands.teleport;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATFloodgatePlayer;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.api.TeleportRequestType;
import io.github.niestrat99.advancedteleport.api.events.players.TeleportRequestEvent;
import io.github.niestrat99.advancedteleport.commands.TeleportATCommand;
import io.github.niestrat99.advancedteleport.commands.TimedATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.github.niestrat99.advancedteleport.utilities.ConditionChecker;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class Tpa extends TeleportATCommand implements TimedATCommand {

    @Override
    public boolean onCommand(
        @NotNull final CommandSender sender,
        @NotNull final Command command,
        @NotNull final String s,
        @NotNull final String[] args
    ) {
        if (!canProceed(sender)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            ATPlayer atPlayer = ATPlayer.getPlayer(player);
            if (atPlayer instanceof ATFloodgatePlayer && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) atPlayer).sendTPAForm(false);
            } else {
                CustomMessages.sendMessage(sender, "Error.noPlayerInput");
            }
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        String result = ConditionChecker.canTeleport(player, target, "tpa");
        if (result != null) {
            CustomMessages.sendMessage(player, result,
                    Placeholder.unparsed("player", args[0]),
                    Placeholder.unparsed("world", target == null ? "<No Such World>" : target.getWorld().getName())
            );
            return true;
        }
        if (PaymentManager.getInstance().canPay("tpa", player)) {
            int requestLifetime = MainConfig.get().REQUEST_LIFETIME.get();
            TeleportRequestEvent event = new TeleportRequestEvent(target, player, TeleportRequestType.TPA);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                // Cannot send request
                return true;
            }

            CustomMessages.sendMessage(sender, "Info.requestSent",
                    Placeholder.parsed("player", MiniMessage.miniMessage().escapeTags(target.getName())),
                    Placeholder.unparsed("lifetime", String.valueOf(requestLifetime))
            );

            CoreClass.playSound("tpa", "sent", player);

            ATPlayer targetPlayer = ATPlayer.getPlayer(target);

            if (targetPlayer instanceof ATFloodgatePlayer && MainConfig.get().USE_FLOODGATE_FORMS.get()) {
                ((ATFloodgatePlayer) targetPlayer).sendRequestFormTPA(player);
            } else {
                CustomMessages.sendMessage(target, "Info.tpaRequestReceived",
                        Placeholder.parsed("player", MiniMessage.miniMessage().escapeTags(sender.getName())),
                        Placeholder.unparsed("lifetime", String.valueOf(requestLifetime))
                );
            }

            CoreClass.playSound("tpa", "received", target);

            BukkitRunnable run = new BukkitRunnable() {
                    @Override
                    public void run() {

                        // Get the teleport request
                        TeleportRequest request = TeleportRequest.getRequestByReqAndResponder(target, player);
                        if (request == null) return;

                        if (MainConfig.get().NOTIFY_ON_EXPIRE.get()) {
                            CustomMessages.sendMessage(sender, "Error.requestExpired",
                                    Placeholder.unparsed("player", target.getName()));
                        }
                        TeleportRequest.removeRequest(request);
                    }
           };
           run.runTaskLater(CoreClass.getInstance(), requestLifetime * 20L); // 60 seconds
           TeleportRequest request = new TeleportRequest(player, target, run, TeleportRequestType.TPA);
           // Creates a new teleport request.
           TeleportRequest.addRequest(request);
           // If the cooldown is to be applied after request, apply it now
           if (MainConfig.get().APPLY_COOLDOWN_AFTER.get().equalsIgnoreCase("request")) {
               CooldownManager.addToCooldown("tpa", player);
           }
        }
        return true;
    }

    @Override
    public @NotNull String getPermission() {
        return "at.member.tpa";
    }

    @Override
    public @NotNull String getSection() {
        return "tpa";
    }
}
