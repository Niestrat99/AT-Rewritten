package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.TeleportRequest;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.limitations.LimitationsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConditionChecker {

    /**
     * Determines whether a player is capable of teleporting to the target player.
     *
     * @param player  the player requesting the teleport
     * @param target  the player being teleported to
     * @param command the command being used
     * @return if the string is empty, the player can teleport, otherwise, state why
     */
    @Contract("_, null, _ -> !null; _, !null, _ -> _")
    public static @Nullable String canTeleport(
        Player player,
        Player target,
        String command
    ) {

        // If the target is null, don't teleport
        if (target == null) return "Error.noSuchPlayer";

        // If the target can't be seen, don't teleport
        if (!player.canSee(target)) return "Error.noSuchPlayer";

        // Are you serious rn
        if (target == player) return "Error.requestSentToSelf";

        // If the player can't be seen, check permissions
        if (!player.hasPermission("at.admin.request-in-vanish") && !target.canSee(player))
            return "Error.cantTPToPlayer";

        // Check if the distance/worlds are a limit
        // if someone removes this for uk and germany that would be great
        String teleportLims = canTeleport(player.getLocation(), target.getLocation(), command, player);
        if (teleportLims != null) return teleportLims;
        ATPlayer atTarget = ATPlayer.getPlayer(target);

        // If the target has teleportation disabled
        if (!atTarget.isTeleportationEnabled()) return "Error.tpOff";

        // Check if the player is blocked
        if (atTarget.hasBlocked(player)) return "Error.tpBlock";

        // If a request has already been sent
        if (command.equalsIgnoreCase("tpa") || command.equalsIgnoreCase("tpahere")) {
            if (TeleportRequest.getRequestByReqAndResponder(target, player) != null) return "Error.alreadySentRequest";
        }
        return null;
    }

    public static @Nullable String canTeleport(
        Location fromLoc,
        Location toLoc,
        String command,
        Player teleportingPlayer
    ) {

        // Use debug print
        CoreClass.debug("Requested to see if " + teleportingPlayer.getName() + " can teleport from "
            + CoreClass.getShortLocation(fromLoc) + " to " + CoreClass.getShortLocation(toLoc) + " using command "
            + command);

        // Check if the player is too far away
        if (MainConfig.get().ENABLE_DISTANCE_LIMITATIONS.get()
                && !teleportingPlayer.hasPermission("at.admin.bypass.distance-limit")
                && fromLoc.getWorld() == toLoc.getWorld()
                && !DistanceLimiter.canTeleport(toLoc, fromLoc, command, ATPlayer.getPlayer(teleportingPlayer))) {
            return "Error.tooFarAway";
        }

        // Check if the player is able to teleport between/within worlds
        if (MainConfig.get().ENABLE_TELEPORT_LIMITATIONS.get()
                && !teleportingPlayer.hasPermission("at.admin.bypass.teleport-limit")) {
            CoreClass.debug("Teleportation limits are enabled and the player is not bypassing them.");
            if (!MainConfig.get().MONITOR_ALL_TELEPORTS_LIMITS.get() && command == null) return null;
            if (!LimitationsManager.canTeleport(teleportingPlayer, toLoc, command)) {
                return "Error.cantTPToWorldLim";
            }
        }
        return null;
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

    public static <T> T validate(
        T object,
        String message
    ) {
        if (object != null) return object;
        throw new NullPointerException(message);
    }
}
