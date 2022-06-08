package org.bukkit.craftbukkit.v1_19_R1.entity;

import net.minecraft.world.entity.LightningBolt;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;

public class CraftLightningStrike extends CraftEntity implements LightningStrike {
    public CraftLightningStrike(final CraftServer server, final LightningBolt entity) {
        super(server, entity);
    }

    @Override
    public boolean isEffect() {
        return this.getHandle().visualOnly;
    }

    @Override
    public LightningBolt getHandle() {
        return (LightningBolt) entity;
    }

    @Override
    public String toString() {
        return "CraftLightningStrike";
    }

    @Override
    public EntityType getType() {
        return EntityType.LIGHTNING;
    }

    // Spigot start
    private final LightningStrike.Spigot spigot = new LightningStrike.Spigot() {

        @Override
        public boolean isSilent()
        {
            return CraftLightningStrike.this.getHandle().isSilent;
        }
    };

    @Override
    public LightningStrike.Spigot spigot() {
        return this.spigot;
    }
    // Spigot end

    // Paper start
    @Override
    public int getFlashCount() {
        return getHandle().flashes;
    }

    @Override
    public void setFlashCount(int flashes) {
        com.google.common.base.Preconditions.checkArgument(flashes >= 0, "Flashes has to be a positive number!");
        getHandle().flashes = flashes;
    }

    @Override
    public int getLifeTicks() {
        return getHandle().life;
    }

    @Override
    public void setLifeTicks(int lifeTicks) {
        getHandle().life = lifeTicks;
    }

    @Override
    public @org.jetbrains.annotations.Nullable org.bukkit.entity.Entity getCausingEntity() {
        final var cause = this.getHandle().getCause();
        return cause == null ? null : cause.getBukkitEntity();
    }

    @Override
    public void setCausingPlayer(@org.jetbrains.annotations.Nullable org.bukkit.entity.Player causingPlayer) {
        this.getHandle().setCause(causingPlayer == null ? null : ((CraftPlayer) causingPlayer).getHandle());
    }
    // Paper end
}
