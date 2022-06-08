package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Set;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.boss.CraftDragonBattle;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.EntityType;

public class CraftEnderDragon extends CraftMob implements EnderDragon {

    public CraftEnderDragon(CraftServer server, net.minecraft.world.entity.boss.enderdragon.EnderDragon entity) {
        super(server, entity);
    }

    @Override
    public Set<ComplexEntityPart> getParts() {
        Builder<ComplexEntityPart> builder = ImmutableSet.builder();

        for (EnderDragonPart part : this.getHandle().subEntities) {
            builder.add((ComplexEntityPart) part.getBukkitEntity());
        }

        return builder.build();
    }

    @Override
    public net.minecraft.world.entity.boss.enderdragon.EnderDragon getHandle() {
        return (net.minecraft.world.entity.boss.enderdragon.EnderDragon) entity;
    }

    @Override
    public String toString() {
        return "CraftEnderDragon";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDER_DRAGON;
    }

    @Override
    public Phase getPhase() {
        return Phase.values()[this.getHandle().getEntityData().get(net.minecraft.world.entity.boss.enderdragon.EnderDragon.DATA_PHASE)];
    }

    @Override
    public void setPhase(Phase phase) {
        this.getHandle().getPhaseManager().setPhase(CraftEnderDragon.getMinecraftPhase(phase));
    }

    public static Phase getBukkitPhase(EnderDragonPhase phase) {
        return Phase.values()[phase.getId()];
    }

    public static EnderDragonPhase getMinecraftPhase(Phase phase) {
        return EnderDragonPhase.getById(phase.ordinal());
    }

    @Override
    public BossBar getBossBar() {
        DragonBattle battle = this.getDragonBattle();
        return battle != null ? battle.getBossBar() : null;
    }

    @Override
    public DragonBattle getDragonBattle() {
        return this.getHandle().getDragonFight() != null ? new CraftDragonBattle(this.getHandle().getDragonFight()) : null;
    }

    @Override
    public int getDeathAnimationTicks() {
        return this.getHandle().dragonDeathTime;
    }

    // Paper start
    @Override
    public org.bukkit.Location getPodium() {
        net.minecraft.core.BlockPos blockPosOrigin = this.getHandle().getPodium();
        return new org.bukkit.Location(getWorld(), blockPosOrigin.getX(), blockPosOrigin.getY(), blockPosOrigin.getZ());
    }

    @Override
    public void setPodium(org.bukkit.Location location) {
        if (location == null) {
            this.getHandle().setPodium(null);
        } else {
            org.apache.commons.lang.Validate.isTrue(location.getWorld() == null || location.getWorld().equals(getWorld()), "You cannot set a podium in a different world to where the dragon is");
            this.getHandle().setPodium(new net.minecraft.core.BlockPos(location.getX(), location.getY(), location.getZ()));
        }
    }
    // Paper end
}
