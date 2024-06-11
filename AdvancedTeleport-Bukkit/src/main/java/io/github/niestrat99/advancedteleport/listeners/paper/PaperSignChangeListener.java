package io.github.niestrat99.advancedteleport.listeners.paper;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

public class PaperSignChangeListener implements Listener {

    @EventHandler
    public void onSignChange(final @NotNull SignChangeEvent event) {

        final Block block = event.getBlock();
        final BlockState state = block.getState();
        final Player player = event.getPlayer();

        // Make sure it's a sign
        if (!(state instanceof Sign sign)) return;

        // Get the line and ensure it's not null
        final var line = event.line(0);
        if (!(line instanceof TextComponent component)) return;

        // Get the sign and ensure it can be created
        final var atSign = AdvancedTeleportAPI.getSignByFlatDisplayName(component);
        if (atSign == null) return;
        if (!atSign.isEnabled()) return;
        if (player.hasPermission(atSign.getAdminPermission())) {
            if (!atSign.canCreate(sign, player)) return;
            event.line(0, atSign.getDisplayName());
        } else {
            CustomMessages.sendMessage(player, "Error.noPermissionSign");
            event.setCancelled(true);
        }
    }
}
