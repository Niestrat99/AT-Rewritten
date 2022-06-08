package org.bukkit.craftbukkit.v1_19_R1.block.data;

import org.bukkit.block.data.Orientable;

public class CraftOrientable extends CraftBlockData implements Orientable {

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> AXIS = getEnum("axis");

    @Override
    public org.bukkit.Axis getAxis() {
        return get(CraftOrientable.AXIS, org.bukkit.Axis.class);
    }

    @Override
    public void setAxis(org.bukkit.Axis axis) {
        set(CraftOrientable.AXIS, axis);
    }

    @Override
    public java.util.Set<org.bukkit.Axis> getAxes() {
        return getValues(CraftOrientable.AXIS, org.bukkit.Axis.class);
    }
}
