package org.bukkit.craftbukkit.v1_19_R1.block.data.type;

import org.bukkit.block.data.type.Farmland;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;

public abstract class CraftFarmland extends CraftBlockData implements Farmland {

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty MOISTURE = getInteger("moisture");

    @Override
    public int getMoisture() {
        return get(CraftFarmland.MOISTURE);
    }

    @Override
    public void setMoisture(int moisture) {
        set(CraftFarmland.MOISTURE, moisture);
    }

    @Override
    public int getMaximumMoisture() {
        return getMax(CraftFarmland.MOISTURE);
    }
}
