package org.bukkit.craftbukkit.v1_19_R1.entity;

import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Turtle;

public class CraftTurtle extends CraftAnimals implements Turtle {

    public CraftTurtle(CraftServer server, net.minecraft.world.entity.animal.Turtle entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.Turtle getHandle() {
        return (net.minecraft.world.entity.animal.Turtle) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftTurtle";
    }

    @Override
    public EntityType getType() {
        return EntityType.TURTLE;
    }

    @Override
    public boolean hasEgg() {
        return this.getHandle().hasEgg();
    }

    @Override
    public boolean isLayingEgg() {
        return this.getHandle().isLayingEgg();
    }

    // Paper start
    @Override
    public org.bukkit.Location getHome() {
        return io.papermc.paper.util.MCUtil.toLocation(getHandle().getLevel(), getHandle().getHomePos());
    }

    @Override
    public void setHome(org.bukkit.Location location) {
        getHandle().setHomePos(io.papermc.paper.util.MCUtil.toBlockPosition(location));
    }

    @Override
    public boolean isGoingHome() {
        return getHandle().isGoingHome();
    }

    @Override
    public boolean isDigging() {
        return getHandle().isLayingEgg();
    }

    @Override
    public void setHasEgg(boolean hasEgg) {
        getHandle().setHasEgg(hasEgg);
    }
    // Paper end
}
