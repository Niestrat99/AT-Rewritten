/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_19_R1.block.impl;

public final class CraftStoneButton extends org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData implements org.bukkit.block.data.type.Switch, org.bukkit.block.data.Directional, org.bukkit.block.data.FaceAttachable, org.bukkit.block.data.Powerable {

    public CraftStoneButton() {
        super();
    }

    public CraftStoneButton(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.type.CraftSwitch

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACE = getEnum(net.minecraft.world.level.block.StoneButtonBlock.class, "face");

    @Override
    public org.bukkit.block.data.type.Switch.Face getFace() {
        return get(CraftStoneButton.FACE, org.bukkit.block.data.type.Switch.Face.class);
    }

    @Override
    public void setFace(org.bukkit.block.data.type.Switch.Face face) {
        set(CraftStoneButton.FACE, face);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.StoneButtonBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return get(CraftStoneButton.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        set(CraftStoneButton.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return getValues(CraftStoneButton.FACING, org.bukkit.block.BlockFace.class);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftFaceAttachable

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> ATTACH_FACE = getEnum(net.minecraft.world.level.block.StoneButtonBlock.class, "face");

    @Override
    public org.bukkit.block.data.FaceAttachable.AttachedFace getAttachedFace() {
        return get(CraftStoneButton.ATTACH_FACE, org.bukkit.block.data.FaceAttachable.AttachedFace.class);
    }

    @Override
    public void setAttachedFace(org.bukkit.block.data.FaceAttachable.AttachedFace face) {
        set(CraftStoneButton.ATTACH_FACE, face);
    }

    // org.bukkit.craftbukkit.v1_19_R1.block.data.CraftPowerable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean(net.minecraft.world.level.block.StoneButtonBlock.class, "powered");

    @Override
    public boolean isPowered() {
        return get(CraftStoneButton.POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(CraftStoneButton.POWERED, powered);
    }
}
