package org.bukkit.craftbukkit.v1_19_R1.block.data.type;

import org.bukkit.block.data.type.Scaffolding;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;

public abstract class CraftScaffolding extends CraftBlockData implements Scaffolding {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty BOTTOM = getBoolean("bottom");
    private static final net.minecraft.world.level.block.state.properties.IntegerProperty DISTANCE = getInteger("distance");

    @Override
    public boolean isBottom() {
        return get(CraftScaffolding.BOTTOM);
    }

    @Override
    public void setBottom(boolean bottom) {
        set(CraftScaffolding.BOTTOM, bottom);
    }

    @Override
    public int getDistance() {
        return get(CraftScaffolding.DISTANCE);
    }

    @Override
    public void setDistance(int distance) {
        set(CraftScaffolding.DISTANCE, distance);
    }

    @Override
    public int getMaximumDistance() {
        return getMax(CraftScaffolding.DISTANCE);
    }
}
