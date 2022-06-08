package io.papermc.paper.entity;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R1.CraftSound;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public interface PaperBucketable extends Bucketable {

    net.minecraft.world.entity.animal.Bucketable getHandle();

    @Override
    default boolean isFromBucket() {
        return this.getHandle().fromBucket();
    }

    @Override
    default void setFromBucket(boolean fromBucket) {
        this.getHandle().setFromBucket(fromBucket);
    }

    @Override
    default ItemStack getBaseBucketItem() {
        return CraftItemStack.asBukkitCopy(this.getHandle().getBucketItemStack());
    }

    @Override
    default Sound getPickupSound() {
        return CraftSound.getBukkit(this.getHandle().getPickupSound());
    }
}
