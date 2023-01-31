package io.github.niestrat99.advancedteleport.api.signs;

import io.github.niestrat99.advancedteleport.api.ATPlayer;
import io.github.niestrat99.advancedteleport.api.ATSign;
import io.github.niestrat99.advancedteleport.api.Home;
import io.github.niestrat99.advancedteleport.commands.home.HomeCommand;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomeSign extends ATSign {

    public HomeSign() {
        super("Home", MainConfig.get().USE_HOMES.get());
    }

    @Override
    public void onInteract(
        @NotNull Sign sign,
        @NotNull Player player
    ) {
        ATPlayer atPlayer = ATPlayer.getPlayer(player);

        Home mainHome = atPlayer.getMainHome();
        if (mainHome == null) {
            if (atPlayer.getHomes().size() == 0) return;
            mainHome = atPlayer.getHomes().values().iterator().next();
        }

        HomeCommand.teleport(player, mainHome);
    }

    @Override
    public boolean canCreate(
        @NotNull Sign sign,
        @NotNull Player player
    ) {
        return true;
    }
}
