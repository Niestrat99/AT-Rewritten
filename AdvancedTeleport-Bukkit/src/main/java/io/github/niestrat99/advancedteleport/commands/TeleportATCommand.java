package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.NewConfig;

public abstract class TeleportATCommand implements ATCommand {

    @Override
    public boolean getRequiredFeature() {
        return NewConfig.get().USE_BASIC_TELEPORT_FEATURES.get();
    }
}
