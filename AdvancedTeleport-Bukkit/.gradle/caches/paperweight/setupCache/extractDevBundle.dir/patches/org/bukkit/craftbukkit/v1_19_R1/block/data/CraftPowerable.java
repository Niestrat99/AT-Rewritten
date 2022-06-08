package org.bukkit.craftbukkit.v1_19_R1.block.data;

import org.bukkit.block.data.Powerable;

public abstract class CraftPowerable extends CraftBlockData implements Powerable {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean("powered");

    @Override
    public boolean isPowered() {
        return get(CraftPowerable.POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(CraftPowerable.POWERED, powered);
    }
}
