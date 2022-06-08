package org.bukkit.craftbukkit.v1_19_R1.block.data;

import org.bukkit.block.data.Levelled;

public abstract class CraftLevelled extends CraftBlockData implements Levelled {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty LEVEL = getInteger("level");

    @Override
    public int getLevel() {
        return get(CraftLevelled.LEVEL);
    }

    @Override
    public void setLevel(int level) {
        set(CraftLevelled.LEVEL, level);
    }

    @Override
    public int getMaximumLevel() {
        return getMax(CraftLevelled.LEVEL);
    }
}
