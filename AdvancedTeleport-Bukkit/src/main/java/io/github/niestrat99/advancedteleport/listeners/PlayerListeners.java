package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.MovementManager;
import io.github.niestrat99.advancedteleport.managers.ParticleManager;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(@NotNull final PlayerJoinEvent event) {

        // Don't track if the player is an NPC
        if (event.getPlayer().hasMetadata("NPC")) return;

        // This will load/relog the associated player data
        ATPlayer.relog(event.getPlayer());

        // Update their username if it has been changed
        PlayerSQLManager.get().updatePlayerData(event.getPlayer());

        // If we aren't notifying administrators about new updates
        if (!MainConfig.get().NOTIFY_ADMINS.get()) return;

        // If the player doesn't have the permission to receive update notifications
        if (!event.getPlayer().hasPermission("at.admin.notify")) return;

        // If there's no new update information
        if (CoreClass.getInstance().getUpdateInfo() == null) return;

        // Get the update title
        String title = (String) CoreClass.getInstance().getUpdateInfo()[1];

        // Get the new version
        String newVersion = (String) CoreClass.getInstance().getUpdateInfo()[0];

        // Get the current version
        String currentVersion = CoreClass.getInstance().getDescription().getVersion();

        // let 'em know :D
        CustomMessages.sendMessage(
                event.getPlayer(),
                "Info.updateInfo",
                Placeholder.unparsed("version", currentVersion),
                Placeholder.unparsed("new-version", newVersion),
                Placeholder.unparsed("title", title));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(@NotNull final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("NPC")) return;
        ATPlayer.removePlayer(player);
        if (MovementManager.getMovement().containsKey(player.getUniqueId())) {
            MovementManager.ImprovedRunnable runnable =
                    MovementManager.getMovement().get(player.getUniqueId());
            ParticleManager.removeParticles(event.getPlayer(), runnable.getCommand());
        }
    }
}
