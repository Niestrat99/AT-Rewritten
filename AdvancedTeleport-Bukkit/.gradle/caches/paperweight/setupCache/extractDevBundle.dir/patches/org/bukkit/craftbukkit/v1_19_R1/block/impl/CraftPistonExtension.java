/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftPistonExtension extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.PistonHead, org.bukkit.block.data.type.TechnicalPiston, org.bukkit.block.data.Directional {

    public CraftPistonExtension() {
        super();
    }

    public CraftPistonExtension(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftPistonHead

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty SHORT = getBoolean(net.minecraft.world.level.block.piston.PistonHeadBlock.class, "short");

    @Override
    public boolean isShort() {
        return get(CraftPistonExtension.SHORT);
    }

    @Override
    public void setShort(boolean _short) {
        set(CraftPistonExtension.SHORT, _short);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftTechnicalPiston

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> TYPE = getEnum(net.minecraft.world.level.block.piston.PistonHeadBlock.class, "type");

    @Override
    public org.bukkit.block.data.type.TechnicalPiston.Type getType() {
        return get(CraftPistonExtension.TYPE, org.bukkit.block.data.type.TechnicalPiston.Type.class);
    }

    @Override
    public void setType(org.bukkit.block.data.type.TechnicalPiston.Type type) {
        set(CraftPistonExtension.TYPE, type);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.piston.PistonHeadBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftPistonExtension.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftPistonExtension.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftPistonExtension.FACING, org.bukkit.block.BlockFace.class);
    }
}
