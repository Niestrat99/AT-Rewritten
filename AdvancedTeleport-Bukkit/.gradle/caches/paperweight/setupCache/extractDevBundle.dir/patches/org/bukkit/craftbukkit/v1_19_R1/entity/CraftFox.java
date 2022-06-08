package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Fox.Type;

public class CraftFox extends CraftAnimals implements Fox {

    public CraftFox(CraftServer server, net.minecraft.world.entity.animal.Fox entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.Fox getHandle() {
        return (net.minecraft.world.entity.animal.Fox) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.FOX;
    }

    @Override
    public String toString() {
        return "CraftFox";
    }

    @Override
    public Type getFoxType() {
        return Type.values()[this.getHandle().getFoxType().ordinal()];
    }

    @Override
    public void setFoxType(Type type) {
        Preconditions.checkArgument(type != null, "type");

        this.getHandle().setFoxType(net.minecraft.world.entity.animal.Fox.Type.values()[type.ordinal()]);
    }

    @Override
    public boolean isCrouching() {
        return this.getHandle().isCrouching();
    }

    @Override
    public void setCrouching(boolean crouching) {
        this.getHandle().setIsCrouching(crouching);
    }

    @Override
    public boolean isSitting() {
        return this.getHandle().isSitting();
    }

    @Override
    public void setSitting(boolean sitting) {
        this.getHandle().setSitting(sitting);
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.getHandle().setSleeping(sleeping);
    }

    @Override
    public AnimalTamer getFirstTrustedPlayer() {
        UUID uuid = this.getHandle().getEntityData().get(net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_0).orElse(null);
        if (uuid == null) {
            return null;
        }

        AnimalTamer player = getServer().getPlayer(uuid);
        if (player == null) {
            player = getServer().getOfflinePlayer(uuid);
        }

        return player;
    }

    @Override
    public void setFirstTrustedPlayer(AnimalTamer player) {
        if (player == null && this.getHandle().getEntityData().get(net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_1).isPresent()) {
            throw new IllegalStateException("Must remove second trusted player first");
        }

        this.getHandle().getEntityData().set(net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_0, player == null ? Optional.empty() : Optional.of(player.getUniqueId()));
    }

    @Override
    public AnimalTamer getSecondTrustedPlayer() {
        UUID uuid = this.getHandle().getEntityData().get(net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_1).orElse(null);
        if (uuid == null) {
            return null;
        }

        AnimalTamer player = getServer().getPlayer(uuid);
        if (player == null) {
            player = getServer().getOfflinePlayer(uuid);
        }

        return player;
    }

    @Override
    public void setSecondTrustedPlayer(AnimalTamer player) {
        if (player != null && !this.getHandle().getEntityData().get(net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_0).isPresent()) {
            throw new IllegalStateException("Must add first trusted player first");
        }

        this.getHandle().getEntityData().set(net.minecraft.world.entity.animal.Fox.DATA_TRUSTED_ID_1, player == null ? Optional.empty() : Optional.of(player.getUniqueId()));
    }

    @Override
    public boolean isFaceplanted() {
        return this.getHandle().isFaceplanted();
    }

    // Paper start - Add more fox behavior API
    @Override
    public void setInterested(boolean interested) {
        this.getHandle().setIsInterested(interested);
    }

    @Override
    public boolean isInterested() {
        return this.getHandle().isInterested();
    }

    @Override
    public void setLeaping(boolean leaping) {
        this.getHandle().setIsPouncing(leaping);
    }

    @Override
    public boolean isLeaping() {
        return this.getHandle().isPouncing();
    }

    @Override
    public void setDefending(boolean defending) {
        this.getHandle().setDefending(defending);
    }

    @Override
    public boolean isDefending() {
        return this.getHandle().isDefending();
    }

    @Override
    public void setFaceplanted(boolean faceplanted) {
        this.getHandle().setFaceplanted(faceplanted);
    }
    // Paper end - Add more fox behavior API
}
