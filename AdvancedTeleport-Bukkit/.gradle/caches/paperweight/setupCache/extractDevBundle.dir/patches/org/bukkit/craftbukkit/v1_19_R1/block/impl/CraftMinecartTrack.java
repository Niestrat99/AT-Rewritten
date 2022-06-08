/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftMinecartTrack extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.Rail, org.bukkit.block.data.Waterlogged {

    public CraftMinecartTrack() {
        super();
    }

    public CraftMinecartTrack(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftRail

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> SHAPE = getEnum(net.minecraft.world.level.block.RailBlock.class, "shape");

    @Override
    public org.bukkit.block.data.Rail.Shape getShape() {
        return get(CraftMinecartTrack.SHAPE, org.bukkit.block.data.Rail.Shape.class);
    }

    @Override
    public void setShape(org.bukkit.block.data.Rail.Shape shape) {
        set(CraftMinecartTrack.SHAPE, shape);
    }

    @Override
    public java.util.Set<org.bukkit.block.data.Rail.Shape> getShapes() {
        return getValues(CraftMinecartTrack.SHAPE, org.bukkit.block.data.Rail.Shape.class);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.RailBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(CraftMinecartTrack.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(CraftMinecartTrack.WATERLOGGED, waterlogged);
    }
}
