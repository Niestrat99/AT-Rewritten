package io.github.niestrat99.advancedteleport.commands;

import io.github.niestrat99.advancedteleport.config.MainConfig;

public abstract class SpawnATCommand extends ATCommand {

    @Override
    public boolean getRequiredFeature() {
        return MainConfig.get().USE_SPAWN.get();
    }
}
