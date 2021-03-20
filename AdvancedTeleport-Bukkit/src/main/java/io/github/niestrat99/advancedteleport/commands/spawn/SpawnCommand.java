package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.ATCommand;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.Spawn;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnCommand implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (NewConfig.get().USE_SPAWN.get()) {
            if (sender.hasPermission("at.member.spawn")){
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    int cooldown = CooldownManager.secondsLeftOnCooldown("spawn", player);
                    if (cooldown > 0) {
                        CustomMessages.sendMessage(sender, "Error.onCooldown", "{time}", String.valueOf(cooldown));
                        return true;
                    }
                    if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
                        CustomMessages.sendMessage(sender, "Error.onCountdown");
                        return true;
                    }
                    spawn(player);
                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
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

        ATPlayer.getPlayer(player).teleport(event, "spawn", "Teleport.teleportingToSpawn", NewConfig.get().WARM_UPS.SPAWN.get());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
