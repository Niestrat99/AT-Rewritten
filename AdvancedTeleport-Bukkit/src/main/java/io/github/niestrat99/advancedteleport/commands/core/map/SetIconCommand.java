package io.github.niestrat99.advancedteleport.commands.core.map;

import io.github.niestrat99.advancedteleport.managers.MapAssetManager;

public class SetIconCommand extends AbstractMapCommand {

    public SetIconCommand() {
        super("map_icon", "Info.setIconSuccess", "Error.setIconFail", MapAssetManager.getImageNames());
    }
}
