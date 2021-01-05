package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.commands.teleport.TpOff;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.config.TpBlock;
import io.github.niestrat99.advancedteleport.limitations.LimitationsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ConditionChecker {

    /**
     * Determines whether a player is capable of teleporting to the target player.
     *
     * @param player the player requesting the teleport
     * @param target the player being teleported to
     * @param command the command being used
     * @return if the string is empty, the player can teleport, otherwise, state why
     */
    public static String canTeleport(Player player, Player target, String command) {
        // If the target is null, don't teleport
        if (target == null) return CustomMessages.getString("Error.noSuchPlayer");
        // If the target can't be seen, don't teleport
        if (!player.canSee(target)) return CustomMessages.getString("Error.noSuchPlayer");
        // Are you serious rn
        if (target == player) return CustomMessages.getString("Error.requestSentToSelf");
        // If the player can't be seen, check permissions
        if (!player.hasPermission("at.admin.request-in-vanish") && !target.canSee(player)) return CustomMessages.getString("Error.cantTPToPlayer");
        // Check if the distance/worlds are a limit
        // if someone removes this for uk and germany that would be great
        String teleportLims = canTeleport(player.getLocation(), target.getLocation(), command, player);
        if (!teleportLims.isEmpty()) return teleportLims;
        // If the target has teleportation disabled
        if (TpOff.getTpOff().contains(target.getUniqueId())) return CustomMessages.getString("Error.tpOff").replaceAll("\\{player}", target.getName());
        // Check if the player is blocked
        if (TpBlock.getBlockedPlayers(target).contains(player.getUniqueId())) return CustomMessages.getString("Error.tpBlock").replaceAll("\\{player}", target.getName());
        // If a request has already been sent
        if (command.equalsIgnoreCase("tpa") || command.equalsIgnoreCase("tpahere")) {
            if (TPRequest.getRequestByReqAndResponder(target, player) != null) return CustomMessages.getString("Error.alreadySentRequest").replaceAll("\\{player}", target.getName());
        }
        return "";
    }

    public static String canTeleport(Location fromLoc, Location toLoc, String command, Player teleportingPlayer) {
        // Check if the player is too far away
        if (NewConfig.getInstance().ENABLE_DISTANCE_LIMITATIONS.get()) {
            if (!teleportingPlayer.hasPermission("at.admin.bypass.distance-limit")) {
                if (fromLoc.getWorld() == toLoc.getWorld()) {
                    if (!DistanceLimiter.canTeleport(toLoc, fromLoc, command)) {
                        return CustomMessages.getString("Error.tooFarAway");
                    }
                }
            }
        }

        // Check if the player is able to teleport between/within worlds
        if (NewConfig.getInstance().ENABLE_TELEPORT_LIMITATIONS.get()) {
            if (!teleportingPlayer.hasPermission("at.admin.bypass.teleport-limit")) {
                if (!NewConfig.getInstance().MONITOR_ALL_TELEPORTS_LIMITS.get() && command == null) return "";
                if (!LimitationsManager.canTeleport(teleportingPlayer, toLoc, command)) {
                    return CustomMessages.getString("Error.cantTPToWorldLim").replaceAll("\\{world}", toLoc.getWorld().getName());
                }
            }
        }

        return "";
    }

    public static List<String> getPlayers(Player player) {
        List<String> players = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player.canSee(target)) {
                players.add(target.getName());
            }
        }
        return players;
    }
}
