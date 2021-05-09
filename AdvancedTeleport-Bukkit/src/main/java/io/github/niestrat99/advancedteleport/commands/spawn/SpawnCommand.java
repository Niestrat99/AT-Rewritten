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
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand implements ATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
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
                    String location = null;
                    if (args.length > 0 && player.hasPermission("at.admin.spawn")) {
                        if (args[0].matches("^[0-9A-Za-z\\-_]+$")) {
                            location = args[0];
                        }
                    }
                    spawn(player, location);
                } else {
                    CustomMessages.sendMessage(sender, "Error.notAPlayer");
                }
            }
        } else {
            CustomMessages.sendMessage(sender, "Error.featureDisabled");
        }
        return true;
    }

    public static void spawn(Player player, String name) {
        Location spawn;
        if (name == null) {
            spawn = Spawn.get().getSpawn(player);
        } else {
            spawn = Spawn.get().getSpawn(name);
        }

        if (spawn == null) {
            spawn = player.getWorld().getSpawnLocation();
        }

        ATTeleportEvent event = new ATTeleportEvent(player, spawn, player.getLocation(), "spawn", ATTeleportEvent.TeleportType.SPAWN);

        ATPlayer.getPlayer(player).teleport(event, "spawn", "Teleport.teleportingToSpawn", NewConfig.get().WARM_UPS.SPAWN.get());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender.hasPermission("at.admin.spawn") && sender instanceof Player && args.length == 1) {
            List<String> spawns = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Spawn.get().getSpawns(), spawns);
            return spawns;
        }
        return null;
    }
}
