/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftTurtleEgg extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.TurtleEgg {

    public CraftTurtleEgg() {
        super();
    }

    public CraftTurtleEgg(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftTurtleEgg

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty EGGS = getInteger(net.minecraft.world.level.block.TurtleEggBlock.class, "eggs");
    private static final net.minecraft.world.level.block.state.properties.IntegerProperty HATCH = getInteger(net.minecraft.world.level.block.TurtleEggBlock.class, "hatch");

    @Override
    public int getEggs() {
        return get(CraftTurtleEgg.EGGS);
    }

    @Override
    public void setEggs(int eggs) {
        set(CraftTurtleEgg.EGGS, eggs);
    }

    @Override
    public int getMinimumEggs() {
        return getMin(CraftTurtleEgg.EGGS);
    }

    @Override
    public int getMaximumEggs() {
        return getMax(CraftTurtleEgg.EGGS);
    }

    @Override
    public int getHatch() {
        return get(CraftTurtleEgg.HATCH);
    }

    @Override
    public void setHatch(int hatch) {
        set(CraftTurtleEgg.HATCH, hatch);
    }

    @Override
    public int getMaximumHatch() {
        return getMax(CraftTurtleEgg.HATCH);
    }
}
