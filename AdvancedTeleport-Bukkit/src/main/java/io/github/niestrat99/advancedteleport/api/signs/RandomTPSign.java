package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.commands.teleport.Tpr;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RandomTPSign extends ATSign {

    public RandomTPSign() {
        super("RandomTP", MainConfig.get().USE_RANDOMTP.get());
    }

    @Override
    public void onInteract(Sign sign, @NotNull Player player) {
        if (!sign.getLine(1).isEmpty()) {
            World otherWorld = Bukkit.getWorld(sign.getLine(1));
            if (otherWorld != null) {
                Tpr.randomTeleport(player, otherWorld);
            } else {
                CustomMessages.sendMessage(player, "Error.noSuchWorld");
            }
        } else {
            Tpr.randomTeleport(player, player.getWorld());
        }
    }

    @Override
    public boolean canCreate(Sign sign, @NotNull Player player) {
        if (!sign.getLine(1).isEmpty()) {
            World otherWorld = Bukkit.getWorld(sign.getLine(1));
            if (otherWorld == null) {
                CustomMessages.sendMessage(player, "Error.noSuchWorld");
                return false;
            }
        }
        return true;
    }
}
