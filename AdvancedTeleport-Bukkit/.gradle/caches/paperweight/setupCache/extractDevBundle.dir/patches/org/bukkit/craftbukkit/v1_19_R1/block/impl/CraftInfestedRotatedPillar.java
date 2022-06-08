/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftInfestedRotatedPillar extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.Orientable {

    public CraftInfestedRotatedPillar() {
        super();
    }

    public CraftInfestedRotatedPillar(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftOrientable

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> AXIS = getEnum(net.minecraft.world.level.block.InfestedRotatedPillarBlock.class, "axis");

    @Override
    public org.bukkit.Axis getAxis() {
        return get(CraftInfestedRotatedPillar.AXIS, org.bukkit.Axis.class);
    }

    @Override
    public void setAxis(org.bukkit.Axis axis) {
        set(CraftInfestedRotatedPillar.AXIS, axis);
    }

    @Override
    public java.util.Set<org.bukkit.Axis> getAxes() {
        return getValues(CraftInfestedRotatedPillar.AXIS, org.bukkit.Axis.class);
    }
}
