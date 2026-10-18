package io.github.niestrat99.advancedteleport.commands.core.map;

import io.github.niestrat99.advancedteleport.managers.MapAssetManager;

public final class SetIconCommand extends AbstractMapCommand {

    public SetIconCommand() {
        super("map_icon", "Info.mapIconUpdateIcon", MapAssetManager.getImageNames());
    }
}
