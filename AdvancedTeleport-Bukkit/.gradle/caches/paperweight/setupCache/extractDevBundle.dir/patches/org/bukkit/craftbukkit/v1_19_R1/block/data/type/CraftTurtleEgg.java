package org.bukkit.craftbukkit.v1_19_R1.block.data.type;

import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;

public abstract class CraftTurtleEgg extends CraftBlockData implements TurtleEgg {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty EGGS = getInteger("eggs");
    private static final net.minecraft.world.level.block.state.properties.IntegerProperty HATCH = getInteger("hatch");

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
