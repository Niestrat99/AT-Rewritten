package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.commands.teleport.Tpr;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RandomTPSign extends ATSign {

    public RandomTPSign() {
        super("RandomTP", MainConfig.get().USE_RANDOMTP.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {
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
    public boolean canCreate(final @NotNull List<Component> lines, final @NotNull Player player) {
        if (!(lines.get(1) instanceof TextComponent line)) return false;
        if (!line.content().isEmpty()) {
            World otherWorld = Bukkit.getWorld(line.content());
            if (otherWorld == null) {
                CustomMessages.sendMessage(player, "Error.noSuchWorld");
                return false;
            }
        }
        return true;
    }
}
