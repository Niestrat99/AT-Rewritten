package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.MainConfig;

public abstract class TeleportATCommand extends ATCommand {

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_BASIC_TELEPORT_FEATURES.get();
    }
}
