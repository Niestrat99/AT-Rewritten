package io.github.niestrat99.advancedteleport.commands.core.map;

public class SetVisibleCommand extends AbstractMapCommand {

    public SetVisibleCommand() {
        super("map_visibility", "Info.mapIconUpdateVisibility", "true", "false");
    }
}
