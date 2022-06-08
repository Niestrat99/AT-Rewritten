package org.bukkit.craftbukkit.v1_19_R1.entity;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.ItemStack;

public abstract class CraftThrowableProjectile extends CraftProjectile implements ThrowableProjectile {

    public CraftThrowableProjectile(CraftServer server, ThrowableItemProjectile entity) {
        super(server, entity);
    }

    @Override
    public ItemStack getItem() {
        if (this.getHandle().getItemRaw().isEmpty()) {
            return CraftItemStack.asBukkitCopy(new net.minecraft.world.item.ItemStack(this.getHandle().getDefaultItemPublic()));
        } else {
            return CraftItemStack.asBukkitCopy(this.getHandle().getItemRaw());
        }
    }

    @Override
    public void setItem(ItemStack item) {
        this.getHandle().setItem(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public ThrowableItemProjectile getHandle() {
        return (ThrowableItemProjectile) entity;
    }
}
