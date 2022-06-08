package org.bukkit.craftbukkit.v1_19_R1.entity;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class CraftItem extends CraftEntity implements Item {

    // Paper start
    private final static int NO_AGE_TIME = (int) Short.MIN_VALUE;
    private final static int NO_PICKUP_TIME = (int) Short.MAX_VALUE;
    // Paper end

    private final ItemEntity item;

    public CraftItem(CraftServer server, Entity entity, ItemEntity item) {
        super(server, entity);
        this.item = item;
    }

    public CraftItem(CraftServer server, ItemEntity entity) {
        this(server, entity, entity);
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asCraftMirror(this.item.getItem());
    }

    @Override
    public void setItemStack(ItemStack stack) {
        this.item.setItem(CraftItemStack.asNMSCopy(stack));
    }

    @Override
    public int getPickupDelay() {
        return item.pickupDelay;
    }

    @Override
    public void setPickupDelay(int delay) {
        item.pickupDelay = Math.min(delay, Short.MAX_VALUE);
    }

    @Override
    public void setUnlimitedLifetime(boolean unlimited) {
        if (unlimited) {
            // See EntityItem#INFINITE_LIFETIME
            item.age = Short.MIN_VALUE;
        } else {
            item.age = getTicksLived();
        }
    }

    @Override
    public boolean isUnlimitedLifetime() {
        return item.age == Short.MIN_VALUE;
    }

    @Override
    public void setTicksLived(int value) {
        super.setTicksLived(value);

        // Second field for EntityItem (don't set if lifetime is unlimited)
        if (!this.isUnlimitedLifetime()) {
            item.age = value;
        }
    }

    // Paper Start
    @Override
    public boolean canMobPickup() {
        return item.canMobPickup;
    }

    @Override
    public void setCanMobPickup(boolean canMobPickup) {
        item.canMobPickup = canMobPickup;
    }

     @Override
     public boolean canPlayerPickup() {
        return item.pickupDelay != NO_PICKUP_TIME;
     }

     @Override
     public void setCanPlayerPickup(boolean canPlayerPickup) {
        item.pickupDelay = canPlayerPickup ? 0 : NO_PICKUP_TIME;
     }

     @Override
     public boolean willAge() {
        return item.age != NO_AGE_TIME;
     }

     @Override
     public void setWillAge(boolean willAge) {
        item.age = willAge ? 0 : NO_AGE_TIME;
     }

    @Override
    public int getHealth() {
        return item.health;
    }

    @Override
    public void setHealth(int health) {
        if (health <= 0) {
            item.getItem().onDestroyed(item);
            item.discard();
        } else {
            item.health = health;
        }
    }
    // Paper End

    @Override
    public void setOwner(UUID uuid) {
        this.item.setOwner(uuid);
    }

    @Override
    public UUID getOwner() {
        return this.item.getOwner();
    }

    @Override
    public void setThrower(UUID uuid) {
        this.item.setThrower(uuid);
    }

    @Override
    public UUID getThrower() {
        return this.item.getThrower();
    }

    @Override
    public String toString() {
        return "CraftItem";
    }

    @Override
    public EntityType getType() {
        return EntityType.DROPPED_ITEM;
    }
}
