package org.bukkit.craftbukkit.v1_19_R1.block.data.type;

import org.bukkit.block.data.type.PistonHead;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;

public abstract class CraftPistonHead extends CraftBlockData implements PistonHead {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty SHORT = getBoolean("short");

    @Override
    public boolean isShort() {
        return get(CraftPistonHead.SHORT);
    }

    @Override
    public void setShort(boolean _short) {
        set(CraftPistonHead.SHORT, _short);
    }
}
