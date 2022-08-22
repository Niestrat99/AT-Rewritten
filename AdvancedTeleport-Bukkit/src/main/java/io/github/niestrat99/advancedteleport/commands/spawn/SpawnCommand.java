package io.github.niestrat99.advancedteleport.commands.spawn;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.events.ATTeleportEvent;
import io.github.niestrat99.advancedteleport.commands.SpawnATCommand;
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

public class SpawnCommand extends SpawnATCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!canProceed(sender)) return true;
        if (!(sender instanceof Player)) {
            CustomMessages.sendMessage(sender, "Error.notAPlayer");
            return true;
        }

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
        String location = player.getWorld().getName();
        if (args.length > 0 &&
                (player.hasPermission("at.admin.spawn") || player.hasPermission("at.member.spawn." + args[0].toLowerCase()))) {
            if (args[0].matches("^[0-9A-Za-z\\-_]+$")) {
                location = args[0];
            }
        }
        spawn(player, location);
        return true;
    }

    public static void spawn(Player player, String name) {

        Location spawn = Spawn.get().getSpawn(name, player, false);
        if (spawn == null) {
            spawn = player.getWorld().getSpawnLocation();
        }

        ATTeleportEvent event = new ATTeleportEvent(player, spawn, player.getLocation(), "spawn",
                ATTeleportEvent.TeleportType.SPAWN);

        ATPlayer.getPlayer(player).teleport(event, "spawn", "Teleport.teleportingToSpawn",
                NewConfig.get().WARM_UPS.SPAWN.get());
    }

    @Override
    public String getPermission() {
        return "at.member.spawn";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                                      @NotNull String[] args) {
        if (sender.hasPermission("at.admin.spawn") && sender instanceof Player && args.length == 1) {
            List<String> spawns = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Spawn.get().getSpawns(), spawns);
            return spawns;
        }
        return null;
    }
}
