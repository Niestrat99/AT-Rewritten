package io.github.niestrat99.advancedteleport.listeners.spigot;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

public class SpigotSignChangeListener implements Listener {

    @EventHandler
    public void onSignChange(final @NotNull SignChangeEvent event) {

        final Block block = event.getBlock();
        final BlockState state = block.getState();
        final Player player = event.getPlayer();

        // Make sure it's a sign
        if (!(state instanceof Sign)) return;

        // Get the line and ensure it's not null
        final var line = event.getLine(0);
        CoreClass.debug("First line of the sign: " + line);
        if (line == null) return;

        // Get the sign and ensure it can be created
        final var atSign = AdvancedTeleportAPI.getSignByFlatDisplayName(Component.text(line));
        CoreClass.debug("Sign found: " + atSign);
        if (atSign == null) return;

        CoreClass.debug("Is sign enabled: " + atSign.isEnabled());
        CoreClass.debug(
                "Does player have permission to create the sign: "
                        + player.hasPermission(atSign.getAdminPermission()));
        CoreClass.debug(
                "Can player create the sign regardless of permission: "
                        + atSign.canCreate(event.getLines(), player));

        if (!atSign.isEnabled()) return;
        if (player.hasPermission(atSign.getAdminPermission())) {
            if (!atSign.canCreate(event.getLines(), player)) return;
            event.setLine(
                    0,
                    LegacyComponentSerializer.legacySection().serialize(atSign.getDisplayName()));
        } else {
            CustomMessages.sendMessage(player, "Error.noPermissionSign");
            event.setCancelled(true);
        }
    }
}
