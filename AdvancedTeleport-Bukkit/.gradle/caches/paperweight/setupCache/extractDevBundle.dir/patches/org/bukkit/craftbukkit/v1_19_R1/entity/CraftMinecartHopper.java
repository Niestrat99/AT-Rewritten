package org.bukkit.craftbukkit.v1_19_R1.entity;

import net.minecraft.world.entity.vehicle.MinecartHopper;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.Inventory;

public final class CraftMinecartHopper extends CraftMinecartContainer implements HopperMinecart, com.destroystokyo.paper.loottable.PaperLootableEntityInventory { // Paper
    private final CraftInventory inventory;

    public CraftMinecartHopper(CraftServer server, MinecartHopper entity) {
        super(server, entity);
        this.inventory = new CraftInventory(entity);
    }

    @Override
    public String toString() {
        return "CraftMinecartHopper{" + "inventory=" + this.inventory + '}';
    }

    @Override
    public EntityType getType() {
        return EntityType.MINECART_HOPPER;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public boolean isEnabled() {
        return ((MinecartHopper) getHandle()).isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        ((MinecartHopper) getHandle()).setEnabled(enabled);
    }
    // Paper start
    @Override
    public net.minecraft.world.entity.vehicle.MinecartHopper getHandle() {
        return (net.minecraft.world.entity.vehicle.MinecartHopper) super.getHandle();
    }

    @Override
    public int getPickupCooldown() {
        return this.getHandle().cooldownTime;
    }

    @Override
    public void setPickupCooldown(int cooldown) {
        this.getHandle().setCooldown(cooldown);
    }
    // Paper end
}
