package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomesSign extends ATSign {

    public HomesSign() {
        super("Homes", NewConfig.get().USE_HOMES.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {

    }

    @Override
    public boolean canCreate(@NotNull Sign sign, @NotNull Player player) {
        return false;
    }
}
