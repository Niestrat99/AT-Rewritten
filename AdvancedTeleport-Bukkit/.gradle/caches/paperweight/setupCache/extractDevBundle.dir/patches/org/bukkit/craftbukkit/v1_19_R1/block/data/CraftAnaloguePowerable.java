package org.bukkit.craftbukkit.v1_19_R1.block.data;

import org.bukkit.block.data.AnaloguePowerable;

public abstract class CraftAnaloguePowerable extends CraftBlockData implements AnaloguePowerable {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty POWER = getInteger("power");

    @Override
    public int getPower() {
        return get(CraftAnaloguePowerable.POWER);
    }

    @Override
    public void setPower(int power) {
        set(CraftAnaloguePowerable.POWER, power);
    }

    @Override
    public int getMaximumPower() {
        return getMax(CraftAnaloguePowerable.POWER);
    }
}
