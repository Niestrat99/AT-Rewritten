/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftCommand extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.CommandBlock, org.bukkit.block.data.Directional {

    public CraftCommand() {
        super();
    }

    public CraftCommand(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftCommandBlock

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty CONDITIONAL = getBoolean(net.minecraft.world.level.block.CommandBlock.class, "conditional");

    @Override
    public boolean isConditional() {
        return get(CraftCommand.CONDITIONAL);
    }

    @Override
    public void setConditional(boolean conditional) {
        set(CraftCommand.CONDITIONAL, conditional);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.CommandBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftCommand.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftCommand.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftCommand.FACING, org.bukkit.block.BlockFace.class);
    }
}
