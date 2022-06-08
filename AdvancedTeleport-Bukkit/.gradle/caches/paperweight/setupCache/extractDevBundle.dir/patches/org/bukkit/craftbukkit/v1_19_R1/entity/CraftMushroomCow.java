package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;

public class CraftMushroomCow extends CraftCow implements MushroomCow {
    public CraftMushroomCow(CraftServer server, net.minecraft.world.entity.animal.MushroomCow entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.MushroomCow getHandle() {
        return (net.minecraft.world.entity.animal.MushroomCow) entity;
    }

    @Override
    public Variant getVariant() {
        return Variant.values()[this.getHandle().getMushroomType().ordinal()];
    }

    @Override
    public void setVariant(Variant variant) {
        Preconditions.checkArgument(variant != null, "variant");

        this.getHandle().setMushroomType(net.minecraft.world.entity.animal.MushroomCow.MushroomType.values()[variant.ordinal()]);
    }

    // Paper start
    @Override
    public int getStewEffectDuration() {
        return this.getHandle().effectDuration;
    }

    @Override
    public void setStewEffectDuration(int duration) {
        this.getHandle().effectDuration = duration;
    }

    @Override
    public org.bukkit.potion.PotionEffectType getStewEffectType() {
        net.minecraft.world.effect.MobEffect effect = this.getHandle().effect;
        if (effect == null) {
            return null;
        }

        return org.bukkit.potion.PotionEffectType.getById(net.minecraft.world.effect.MobEffect.getId(effect));
    }

    @Override
    public void setStewEffect(org.bukkit.potion.PotionEffectType type) {
        net.minecraft.world.effect.MobEffect effect = null;
        if (type != null) {
            effect = net.minecraft.world.effect.MobEffect.byId(type.getId());
        }

        this.getHandle().effect = effect;
    }
    // Paper end

    @Override
    public String toString() {
        return "CraftMushroomCow";
    }

    @Override
    public EntityType getType() {
        return EntityType.MUSHROOM_COW;
    }
}
