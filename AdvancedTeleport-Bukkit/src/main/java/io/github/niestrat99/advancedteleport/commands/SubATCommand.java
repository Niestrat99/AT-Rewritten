package io.github.niestrat99.advancedteleport.commands;

public interface SubATCommand extends ATCommand {

    default String getPermission() {
        return "";
    }

    @Override
    default boolean getRequiredFeature() {
        return true;
    }
}
