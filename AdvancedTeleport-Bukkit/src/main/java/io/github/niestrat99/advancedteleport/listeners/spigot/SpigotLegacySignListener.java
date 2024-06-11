package io.github.niestrat99.advancedteleport.listeners.spigot;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpigotLegacySignListener implements Listener {

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();
        final Block clickedBlock = event.getClickedBlock();
        final BlockState blockState = clickedBlock.getState();

        if (!(blockState instanceof Sign sign)) return;

        final String line = sign.getLine(0);
        final ATSign atSign = AdvancedTeleportAPI.getSignByLegacyName(line);
        if (atSign == null) return;

        // If the sign isn't enabled and the player doesn't have permission to use it
        if (!atSign.isEnabled()) return;
        if (!player.hasPermission(atSign.getRequiredPermission())) return;

        // If there's a cooldown in place, then check that
        int cooldown = CooldownManager.secondsLeftOnCooldown(atSign.getName().toLowerCase(), player);
        if (cooldown > 0) {
            CustomMessages.sendMessage(
                    player,
                    "Error.onCooldown",
                    Placeholder.unparsed("time", String.valueOf(cooldown)));
            return;
        }
        atSign.onInteract(sign, player);
    }
}
