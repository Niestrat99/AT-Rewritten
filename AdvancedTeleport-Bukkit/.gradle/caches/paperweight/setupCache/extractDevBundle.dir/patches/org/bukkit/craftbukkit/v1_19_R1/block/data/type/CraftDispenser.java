package org.bukkit.craftbukkit.v1_19_R1.block.data.type;

import org.bukkit.block.data.type.Dispenser;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;

public abstract class CraftDispenser extends CraftBlockData implements Dispenser {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty TRIGGERED = getBoolean("triggered");

    @Override
    public boolean isTriggered() {
        return get(CraftDispenser.TRIGGERED);
    }

    @Override
    public void setTriggered(boolean triggered) {
        set(CraftDispenser.TRIGGERED, triggered);
    }
}
