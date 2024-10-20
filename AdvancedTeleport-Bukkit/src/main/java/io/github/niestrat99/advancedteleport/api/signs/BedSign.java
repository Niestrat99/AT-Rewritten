package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.commands.home.HomeCommand;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BedSign extends ATSign {

    public BedSign() {
        super("Bed", MainConfig.get().USE_HOMES.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {
        ATPlayer atPlayer = ATPlayer.getPlayer(player);
        if (atPlayer.getBedSpawn() == null) return;
        HomeCommand.teleport(player, atPlayer.getBedSpawn());
    }

    @Override
    public boolean canCreate(final @NotNull List<Component> lines, final @NotNull Player player) {
        return true;
    }
}
