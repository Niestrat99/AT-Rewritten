package io.github.niestrat99.advancedteleport.listeners;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.sql.PlayerSQLManager;
import io.github.niestrat99.advancedteleport.utilities.nbt.NBTReader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasMetadata("NPC")) return;

        // This will load/relog the associated player data
        ATPlayer.relog(event.getPlayer());

        PlayerSQLManager.get().updatePlayerData(event.getPlayer());
        if (!NewConfig.get().NOTIFY_ADMINS.get()) return;
        if (!event.getPlayer().hasPermission("at.admin.notify")) return;
        if (CoreClass.getInstance().getUpdateInfo() == null) return;
        String title = (String) CoreClass.getInstance().getUpdateInfo()[1];
        String newVersion = (String) CoreClass.getInstance().getUpdateInfo()[0];
        String currentVersion = CoreClass.getInstance().getDescription().getVersion();
        CustomMessages.sendMessage(event.getPlayer(), "Info.updateInfo", "{version}", currentVersion,
                "{new-version}", newVersion, "{title}", title);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (event.getPlayer().hasMetadata("NPC")) return;
        ATPlayer.removePlayer(event.getPlayer());
    }


}
