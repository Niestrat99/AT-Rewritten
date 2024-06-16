package io.github.niestrat99.advancedteleport.listeners.paper;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.CoreClass;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PaperLegacySignListener implements Listener {

    @EventHandler
    @SuppressWarnings("deprecated")
    public void onSignInteract(PlayerInteractEvent event) {

	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();
        final Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        final BlockState blockState = clickedBlock.getState();
        
	CoreClass.debug("Block interaction detected for " + player.getName() + ", is it a sign: " + (blockState instanceof Sign));
        if (!(blockState instanceof Sign sign)) return;

        ATSign atSign = AdvancedTeleportAPI.getSignByDisplayName(sign.line(0));
	CoreClass.debug("Obtained sign: " + atSign);
	if (atSign != null)
	    CoreClass.debug("Is sign enabled: " + atSign.isEnabled());

        // If the sign isn't enabled and the player doesn't have permission to use it
        if (atSign == null || !atSign.isEnabled()) return;
	CoreClass.debug("Does player have permission to use the sign: " + player.hasPermission(atSign.getRequiredPermission()));
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
