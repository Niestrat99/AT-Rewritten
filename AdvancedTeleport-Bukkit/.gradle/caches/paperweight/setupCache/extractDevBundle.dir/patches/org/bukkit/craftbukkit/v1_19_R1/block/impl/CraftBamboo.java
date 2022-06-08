/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftBamboo extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.Bamboo, org.bukkit.block.data.Ageable, org.bukkit.block.data.type.Sapling {

    public CraftBamboo() {
        super();
    }

    public CraftBamboo(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftBamboo

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> LEAVES = getEnum(net.minecraft.world.level.block.BambooBlock.class, "leaves");

    @Override
    public org.bukkit.block.data.type.Bamboo.Leaves getLeaves() {
        return get(CraftBamboo.LEAVES, org.bukkit.block.data.type.Bamboo.Leaves.class);
    }

    @Override
    public void setLeaves(org.bukkit.block.data.type.Bamboo.Leaves leaves) {
        set(CraftBamboo.LEAVES, leaves);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftAgeable

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE = getInteger(net.minecraft.world.level.block.BambooBlock.class, "age");

    @Override
    public int getAge() {
        return get(CraftBamboo.AGE);
    }

    @Override
    public void setAge(int age) {
        set(CraftBamboo.AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return getMax(CraftBamboo.AGE);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftSapling

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty STAGE = getInteger(net.minecraft.world.level.block.BambooBlock.class, "stage");

    @Override
    public int getStage() {
        return get(CraftBamboo.STAGE);
    }

    @Override
    public void setStage(int stage) {
        set(CraftBamboo.STAGE, stage);
    }

    @Override
    public int getMaximumStage() {
        return getMax(CraftBamboo.STAGE);
    }
}
