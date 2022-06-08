package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class CraftEnderSignal extends CraftEntity implements EnderSignal {
    public CraftEnderSignal(CraftServer server, EyeOfEnder entity) {
        super(server, entity);
    }

    @Override
    public EyeOfEnder getHandle() {
        return (EyeOfEnder) entity;
    }

    @Override
    public String toString() {
        return "CraftEnderSignal";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDER_SIGNAL;
    }

    @Override
    public Location getTargetLocation() {
        return new Location(getWorld(), this.getHandle().tx, this.getHandle().ty, this.getHandle().tz, this.getHandle().getYRot(), this.getHandle().getXRot());
    }

    @Override
    public void setTargetLocation(Location location) {
        // Paper start
        this.setTargetLocation(location, true);
    }

    @Override
    public void setTargetLocation(Location location, boolean update) {
        // Paper end
        Preconditions.checkArgument(getWorld().equals(location.getWorld()), "Cannot target EnderSignal across worlds");
        this.getHandle().signalTo(new BlockPos(location.getX(), location.getY(), location.getZ()), update); // Paper
    }

    @Override
    public boolean getDropItem() {
        return this.getHandle().surviveAfterDeath;
    }

    @Override
    public void setDropItem(boolean shouldDropItem) {
        this.getHandle().surviveAfterDeath = shouldDropItem;
    }

    @Override
    public ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(this.getHandle().getItem());
    }

    @Override
    public void setItem(ItemStack item) {
        this.getHandle().setItem(item != null ? CraftItemStack.asNMSCopy(item) : Items.ENDER_EYE.getDefaultInstance());
    }

    @Override
    public int getDespawnTimer() {
        return this.getHandle().life;
    }

    @Override
    public void setDespawnTimer(int time) {
        this.getHandle().life = time;
    }
}
