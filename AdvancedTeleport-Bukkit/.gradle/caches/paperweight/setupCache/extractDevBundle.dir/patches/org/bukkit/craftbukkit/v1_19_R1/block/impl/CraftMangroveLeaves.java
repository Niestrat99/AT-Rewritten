/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftMangroveLeaves extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.Leaves, org.bukkit.block.data.Waterlogged {

    public CraftMangroveLeaves() {
        super();
    }

    public CraftMangroveLeaves(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftLeaves

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty DISTANCE = getInteger(net.minecraft.world.level.block.MangroveLeavesBlock.class, "distance");
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty PERSISTENT = getBoolean(net.minecraft.world.level.block.MangroveLeavesBlock.class, "persistent");

    @Override
    public boolean isPersistent() {
        return get(CraftMangroveLeaves.PERSISTENT);
    }

    @Override
    public void setPersistent(boolean persistent) {
        set(CraftMangroveLeaves.PERSISTENT, persistent);
    }

    @Override
    public int getDistance() {
        return get(CraftMangroveLeaves.DISTANCE);
    }

    @Override
    public void setDistance(int distance) {
        set(CraftMangroveLeaves.DISTANCE, distance);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.MangroveLeavesBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(CraftMangroveLeaves.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(CraftMangroveLeaves.WATERLOGGED, waterlogged);
    }

    // Paper start
    @Override
    public int getMinimumDistance() {
        return getMin(CraftMangroveLeaves.DISTANCE);
    }

    @Override
    public int getMaximumDistance() {
        return getMax(CraftMangroveLeaves.DISTANCE);
    }
    // Paper end
}
