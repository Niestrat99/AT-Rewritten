package io.papermc.paper.commands;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.command.CommandBlockHolder;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.BaseCommandBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PaperCommandBlockHolder extends CommandBlockHolder {

    BaseCommandBlock getCommandBlockHandle();

    @Override
    default @NotNull Component lastOutput() {
        return PaperAdventure.asAdventure(getCommandBlockHandle().getLastOutput());
    }

    @Override
    default void lastOutput(@Nullable Component lastOutput) {
        getCommandBlockHandle().setLastOutput(PaperAdventure.asVanilla(lastOutput));
    }

    @Override
    default int getSuccessCount() {
        return getCommandBlockHandle().getSuccessCount();
    }

    @Override
    default void setSuccessCount(int successCount) {
        getCommandBlockHandle().setSuccessCount(successCount);
    }
}
