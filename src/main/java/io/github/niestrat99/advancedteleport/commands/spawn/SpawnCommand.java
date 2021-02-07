package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.payments.PaymentManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnCommand implements ATCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (NewConfig.getInstance().USE_SPAWN.get()) {
            if (sender.hasPermission("at.member.spawn")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int cooldown = CooldownManager.secondsLeftOnCooldown("spawn", player);
                    if (cooldown > 0) {
                        sender.sendMessage(CustomMessages.getString("Error.onCooldown").replaceAll("\\{time}", String.valueOf(cooldown)));
                        return true;
                    }
                    if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                        sender.sendMessage(CustomMessages.getString("Error.onCountdown"));
                        return true;
                    }
                    spawn(player);
                } else {
                    sender.sendMessage(CustomMessages.getString("Error.notAPlayer"));
                }
            }
        } else {
            sender.sendMessage(CustomMessages.getString("Error.featureDisabled"));
        }
        return true;
    }

    public static void spawn(Player player) {
        Location spawn;
        if (Spawn.getSpawnFile() != null) {
            spawn = Spawn.getSpawnFile();
        } else {
            spawn = player.getWorld().getSpawnLocation();
        }
        ATTeleportEvent event = new ATTeleportEvent(player, spawn, player.getLocation(), "spawn", ATTeleportEvent.TeleportType.SPAWN);
        if (!event.isCancelled()) {
            if (PaymentManager.getInstance().canPay("spawn", player)) {
                // If the cooldown is to be applied after request or accept (they are the same in the case of /spawn), apply it now
                String cooldownConfig = NewConfig.getInstance().APPLY_COOLDOWN_AFTER.get();
                if(cooldownConfig.equalsIgnoreCase("request") || cooldownConfig.equalsIgnoreCase("accept")) {
                    CooldownManager.addToCooldown("spawn", player);
                }
                int warmUp = NewConfig.getInstance().WARM_UPS.SPAWN.get();
                if (warmUp > 0 && !player.hasPermission("at.admin.bypass.timer")) {
                    MovementManager.createMovementTimer(player, spawn, "spawn", "Teleport.teleportingToSpawn", warmUp);

                } else {
                    PaymentManager.getInstance().withdraw("spawn", player);
                    PaperLib.teleportAsync(player, spawn, PlayerTeleportEvent.TeleportCause.COMMAND);
                    player.sendMessage(CustomMessages.getString("Teleport.teleportingToSpawn"));
                }
            }
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
