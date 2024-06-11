package io.github.niestrat99.advancedteleport.listeners.spigot;

import io.github.niestrat99.advancedteleport.api.AdvancedTeleportAPI;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.listeners.NewListener;
import io.github.niestrat99.advancedteleport.managers.CooldownManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecated") // being ignored :(
public class SpigotSignOpenListener implements NewListener {

    @EventHandler
    public void onSignOpen(PlayerSignOpenEvent event) {

        if (event.getCause() != PlayerSignOpenEvent.Cause.INTERACT) return;

        // Check the clicked side
        boolean result = checkSide(event.getSide(), event);
        if (!result && !MainConfig.get().TELEPORT_ON_SIGN_SIDE.get()) {
            checkSide(event.getSide() == Side.FRONT ? Side.BACK : Side.FRONT, event);
        }
    }

    private boolean checkSide(final @NotNull Side side, final @NotNull PlayerSignOpenEvent event) {

        // Get the first line of the sign
        final var line = event.getSign().getSide(side).getLine(0);

        // Try and fetch the sign being clicked
        final var sign = AdvancedTeleportAPI.getSignByLegacyName(line);
        if (sign == null) return false;

        // If the player can't use the sign, just ignore
        if (!sign.isEnabled()) return false;
        if (!event.getPlayer().hasPermission(sign.getRequiredPermission())) return false;
        if (event.getPlayer().isSneaking() && event.getPlayer().hasPermission(sign.getAdminPermission())) return true;

        event.setCancelled(true);

        // If there's a cooldown in place, then check that
        int cooldown = CooldownManager.secondsLeftOnCooldown(sign.getName().toLowerCase(), event.getPlayer());
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
            Class.forName(PlayerSignOpenEvent.class.getName());
            return true;
        } catch (NoClassDefFoundError | ClassNotFoundException err) {
            return false;
        }
    }
}
