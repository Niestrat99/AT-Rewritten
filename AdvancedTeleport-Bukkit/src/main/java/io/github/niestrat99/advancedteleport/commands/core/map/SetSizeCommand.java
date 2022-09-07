package io.github.niestrat99.advancedteleport.commands.core.map;

import java.util.Arrays;

public class SetSizeCommand extends AbstractMapCommand {

    public SetSizeCommand(String successMsg, String finalMsg) {
        super("map_size", successMsg, finalMsg, Arrays.asList("8", "16", "32"));
    }
}
