package io.github.niestrat99.advancedteleport.listeners.paper;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.listeners.NewListener;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class PaperSignOpenListener implements NewListener {

    @EventHandler
    public void onPlayerSignOpen(final @NotNull PlayerOpenSignEvent event) {

        // If they're not interacting or sneaking, ignore
	CoreClass.debug("Sign open event detected, cause: " + event.getCause().name());
        if (event.getCause() != PlayerOpenSignEvent.Cause.INTERACT) return;

        // Check the clicked side
        boolean result = checkSide(event.getSide(), event);
        if (!result && !MainConfig.get().TELEPORT_ON_SIGN_SIDE.get()) {
            checkSide(event.getSide() == Side.FRONT ? Side.BACK : Side.FRONT, event);
        }
    }

    private boolean checkSide(final @NotNull Side side, final @NotNull PlayerOpenSignEvent event) {

        // Get the first line of the sign
        final var line = event.getSign().getSide(side).line(0);

	CoreClass.debug("First line: " + line.toString());

        // Try and fetch the sign being clicked
        var sign = AdvancedTeleportAPI.getSignByDisplayName(line);
	CoreClass.debug("Sign found: " + sign);
        if (sign == null) return false;

        // If the player can't use the sign or is editing it as an admin, just ignore
	CoreClass.debug("Is the sign enabled?: " + sign.isEnabled());
	CoreClass.debug("Can the player use the sign?: " + event.getPlayer().hasPermission(sign.getRequiredPermission()));
	CoreClass.debug("Is the player trying to edit the sign?: " + event.getPlayer().isSneaking());
	CoreClass.debug("Is the player able to edit the sign?: " + event.getPlayer().hasPermission(sign.getAdminPermission()));

        if (!sign.isEnabled()) return false;
        if (!event.getPlayer().hasPermission(sign.getRequiredPermission())) return false;
        if (event.getPlayer().isSneaking() && event.getPlayer().hasPermission(sign.getAdminPermission())) return true;

        event.setCancelled(true);

        // If there's a cooldown in place, then check that
        int cooldown = CooldownManager.secondsLeftOnCooldown(sign.getName().toLowerCase(), event.getPlayer());
        CoreClass.debug("Cooldown found for " + event.getPlayer().getName() + " in " + sign.getName().toLowerCase() + ": " + cooldown);
	if (cooldown > 0) {
            CustomMessages.sendMessage(
                    event.getPlayer(),
                    "Error.onCooldown",
                    Placeholder.unparsed("time", String.valueOf(cooldown)));
            return true;
        }

        sign.onInteract(event.getSign(), event.getPlayer());
        return true;
    }

    @Override
    public boolean canRegister() {
        try {
            Class.forName(PlayerOpenSignEvent.class.getName());
            return true;
        } catch (NoClassDefFoundError | ClassNotFoundException err) {
            return false;
        }
    }
}
