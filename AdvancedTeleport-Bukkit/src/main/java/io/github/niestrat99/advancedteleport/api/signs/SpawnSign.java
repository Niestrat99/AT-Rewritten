package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.spawn.Spawn;
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
    public void onInteract(
            @NotNull Sign sign,
            @NotNull Player player
    ) {

        // Get the ID of the proposed spawn
        String spawnName = player.getWorld().getName();
        if (!sign.getLine(1).isEmpty()) spawnName = sign.getLine(1);

        // Get the spawn on its own
        Spawn spawn = NamedLocationManager.get().getSpawn(spawnName);

        // If it's null, get the main spawn
        if (spawn == null) spawn = NamedLocationManager.get().getSpawn(player.getWorld(), player);

        // Teleport them to spawn
        SpawnCommand.spawn(player, spawn);
    }

    @Override
    public boolean canCreate(
        @NotNull Sign sign,
        @NotNull Player player
    ) {
        return true;
    }
}
