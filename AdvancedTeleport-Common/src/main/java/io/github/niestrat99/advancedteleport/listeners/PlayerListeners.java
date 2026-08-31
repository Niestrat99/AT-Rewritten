package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.CoreAdvancedTeleport;
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
        if (event.getPlayer().hasMetadata("NPC")) return;
        ATPlayer.relog(event.getPlayer());
        PlayerSQLManager.get().updatePlayerData(event.getPlayer());
        if (!MainConfig.get().NOTIFY_ADMINS.get()) return;
        if (!event.getPlayer().hasPermission("at.admin.notify")) return;
        if (CoreAdvancedTeleport.getInstance().getAvailableUpdate() == null && CoreAdvancedTeleport.getInstance().getAvailableUpdate().isNewerThanCurrent()) return;
        String newVersion = CoreAdvancedTeleport.getInstance().getAvailableUpdate().versionTag();
        String currentVersion = CoreAdvancedTeleport.getInstance().getPlugin().getDescription().getVersion();
        CustomMessages.sendMessage(
                event.getPlayer(),
                "Info.updateInfo",
                Placeholder.unparsed("version", currentVersion),
                Placeholder.unparsed("new-version", newVersion),
                Placeholder.unparsed("title", "N/A"));
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
