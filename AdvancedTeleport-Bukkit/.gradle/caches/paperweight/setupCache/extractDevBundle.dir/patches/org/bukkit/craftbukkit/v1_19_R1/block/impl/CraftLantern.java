/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftLantern extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.Lantern, org.bukkit.block.data.Hangable, org.bukkit.block.data.Waterlogged {

    public CraftLantern() {
        super();
    }

    public CraftLantern(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftHangable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty HANGING = getBoolean(net.minecraft.world.level.block.LanternBlock.class, "hanging");

    @Override
    public boolean isHanging() {
        return get(CraftLantern.HANGING);
    }

    @Override
    public void setHanging(boolean hanging) {
        set(CraftLantern.HANGING, hanging);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.LanternBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return get(CraftLantern.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        set(CraftLantern.WATERLOGGED, waterlogged);
    }
}
