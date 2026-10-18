package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomesSign extends ATSign {

    public HomesSign() {
        super("Homes", MainConfig.get().USE_HOMES.get());
    }

    @Override
    public void onInteract(@NotNull Sign sign, @NotNull Player player) {}

    @Override
    public boolean canCreate(final @NotNull List<Component> lines, final @NotNull Player player) {
        return false;
    }
}
