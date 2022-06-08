package org.bukkit.craftbukkit.v1_19_R1.block.data.type;

import org.bukkit.block.data.type.Repeater;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;

public abstract class CraftRepeater extends CraftBlockData implements Repeater {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty DELAY = getInteger("delay");
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty LOCKED = getBoolean("locked");

    @Override
    public int getDelay() {
        return get(CraftRepeater.DELAY);
    }

    @Override
    public void setDelay(int delay) {
        set(CraftRepeater.DELAY, delay);
    }

    @Override
    public int getMinimumDelay() {
        return getMin(CraftRepeater.DELAY);
    }

    @Override
    public int getMaximumDelay() {
        return getMax(CraftRepeater.DELAY);
    }

    @Override
    public boolean isLocked() {
        return get(CraftRepeater.LOCKED);
    }

    @Override
    public void setLocked(boolean locked) {
        set(CraftRepeater.LOCKED, locked);
    }
}
