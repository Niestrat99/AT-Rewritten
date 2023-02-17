package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.commands.spawn.SpawnCommand;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.managers.NamedLocationManager;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnSign extends ATSign {

    public SpawnSign() {
        super("Spawn", MainConfig.get().USE_SPAWN.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {
        String world = player.getWorld().getName();
        if (!sign.getLine(1).isEmpty()) {
            world = sign.getLine(1);
        }
        SpawnCommand.spawn(player, world);
    }

    @Override
    public boolean canCreate(@NotNull Sign sign, @NotNull Player player) {
        return true;
    }
}
