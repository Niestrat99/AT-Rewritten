package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryLlama;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.inventory.LlamaInventory;

public class CraftLlama extends CraftChestedHorse implements Llama, com.destroystokyo.paper.entity.CraftRangedEntity<net.minecraft.world.entity.animal.horse.Llama> { // Paper

    public CraftLlama(CraftServer server, net.minecraft.world.entity.animal.horse.Llama entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.horse.Llama getHandle() {
        return (net.minecraft.world.entity.animal.horse.Llama) super.getHandle();
    }

    @Override
    public Color getColor() {
        return Color.values()[this.getHandle().getVariant()];
    }

    @Override
    public void setColor(Color color) {
        Preconditions.checkArgument(color != null, "color");

        this.getHandle().setVariant(color.ordinal());
    }

    @Override
    public LlamaInventory getInventory() {
        return new CraftInventoryLlama(this.getHandle().inventory);
    }

    @Override
    public int getStrength() {
       return this.getHandle().getStrength();
    }

    @Override
    public void setStrength(int strength) {
        Preconditions.checkArgument(1 <= strength && strength <= 5, "strength must be [1,5]");
        if (strength == this.getStrength()) return;
        this.getHandle().setStrengthPublic(strength);
        this.getHandle().createInventory();
    }

    @Override
    public Horse.Variant getVariant() {
        return Horse.Variant.LLAMA;
    }

    @Override
    public String toString() {
        return "CraftLlama";
    }

    @Override
    public EntityType getType() {
        return EntityType.LLAMA;
    }

    // Paper start
    @Override
    public boolean inCaravan() {
        return this.getHandle().inCaravan();
    }

    @Override
    public void joinCaravan(@org.jetbrains.annotations.NotNull Llama llama) {
        this.getHandle().joinCaravan(((CraftLlama) llama).getHandle());
    }

    @Override
    public void leaveCaravan() {
        this.getHandle().leaveCaravan();
    }

    @Override
    public boolean hasCaravanTail() {
        return this.getHandle().hasCaravanTail();
    }

    @Override
    public Llama getCaravanHead() {
        return this.getHandle().getCaravanHead() == null ? null : (Llama) this.getHandle().getCaravanHead().getBukkitEntity();
    }

    @Override
    public Llama getCaravanTail() {
        return this.getHandle().caravanTail == null ? null : (Llama) this.getHandle().caravanTail.getBukkitEntity();
    }
    // Paper end
}
