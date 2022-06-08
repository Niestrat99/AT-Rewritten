package org.bukkit.craftbukkit.v1_19_R1.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;

/**
 * Represents input from a command block
 */
public class CraftBlockCommandSender extends ServerCommandSender implements BlockCommandSender {
    private final CommandSourceStack block;
    private final BlockEntity tile;

    public CraftBlockCommandSender(CommandSourceStack commandBlockListenerAbstract, BlockEntity tile) {
        super();
        this.block = commandBlockListenerAbstract;
        this.tile = tile;
    }

    @Override
    public Block getBlock() {
        return CraftBlock.at(this.tile.getLevel(), this.tile.getBlockPos());
    }

    @Override
    public void sendMessage(String message) {
        for (Component component : CraftChatMessage.fromString(message)) {
            block.source.sendSystemMessage(component);
        }
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            this.sendMessage(message);
        }
    }

    @Override
    public String getName() {
        return this.block.getTextName();
    }

    // Paper start
    @Override
    public void sendMessage(net.kyori.adventure.identity.Identity identity, net.kyori.adventure.text.Component message, net.kyori.adventure.audience.MessageType type) {
        block.source.sendSystemMessage(io.papermc.paper.adventure.PaperAdventure.asVanilla(message));
    }

    @Override
    public net.kyori.adventure.text.Component name() {
        return io.papermc.paper.adventure.PaperAdventure.asAdventure(this.block.getDisplayName());
    }
    // Paper end

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException("Cannot change operator status of a block");
    }

    public CommandSourceStack getWrapper() {
        return this.block;
    }
}
