package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TownSpawnSign extends ATSign {

    public TownSpawnSign() {
        super("TownSpawn", MainConfig.get().USE_TOWNY.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {
        String townName = sign.getLine(1).trim();
        if (townName.isEmpty()) {
            player.performCommand("town spawn");
        } else {
            player.performCommand("town spawn " + townName);
        }
    }

    @Override
    public boolean canCreate(final @NotNull List<Component> lines, final @NotNull Player player) {
        return true;
    }
}
