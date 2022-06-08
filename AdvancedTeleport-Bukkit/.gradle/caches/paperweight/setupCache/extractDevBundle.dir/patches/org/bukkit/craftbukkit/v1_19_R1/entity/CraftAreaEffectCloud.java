package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_19_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.potion.CraftPotionUtil;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public class CraftAreaEffectCloud extends CraftEntity implements AreaEffectCloud {

    public CraftAreaEffectCloud(CraftServer server, net.minecraft.world.entity.AreaEffectCloud entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.AreaEffectCloud getHandle() {
        return (net.minecraft.world.entity.AreaEffectCloud) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftAreaEffectCloud";
    }

    @Override
    public EntityType getType() {
        return EntityType.AREA_EFFECT_CLOUD;
    }

    @Override
    public int getDuration() {
        return this.getHandle().getDuration();
    }

    @Override
    public void setDuration(int duration) {
        this.getHandle().setDuration(duration);
    }

    @Override
    public int getWaitTime() {
        return this.getHandle().waitTime;
    }

    @Override
    public void setWaitTime(int waitTime) {
        this.getHandle().setWaitTime(waitTime);
    }

    @Override
    public int getReapplicationDelay() {
        return this.getHandle().reapplicationDelay;
    }

    @Override
    public void setReapplicationDelay(int delay) {
        this.getHandle().reapplicationDelay = delay;
    }

    @Override
    public int getDurationOnUse() {
        return this.getHandle().durationOnUse;
    }

    @Override
    public void setDurationOnUse(int duration) {
        this.getHandle().durationOnUse = duration;
    }

    @Override
    public float getRadius() {
        return this.getHandle().getRadius();
    }

    @Override
    public void setRadius(float radius) {
        this.getHandle().setRadius(radius);
    }

    @Override
    public float getRadiusOnUse() {
        return this.getHandle().radiusOnUse;
    }

    @Override
    public void setRadiusOnUse(float radius) {
        this.getHandle().setRadiusOnUse(radius);
    }

    @Override
    public float getRadiusPerTick() {
        return this.getHandle().radiusPerTick;
    }

    @Override
    public void setRadiusPerTick(float radius) {
        this.getHandle().setRadiusPerTick(radius);
    }

    @Override
    public Particle getParticle() {
        return CraftParticle.toBukkit(this.getHandle().getParticle());
    }

    @Override
    public void setParticle(Particle particle) {
        this.setParticle(particle, null);
    }

    @Override
    public <T> void setParticle(Particle particle, T data) {
        this.getHandle().setParticle(CraftParticle.toNMS(particle, data));
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(this.getHandle().getColor());
    }

    @Override
    public void setColor(Color color) {
        this.getHandle().setFixedColor(color.asRGB());
    }

    @Override
    public boolean addCustomEffect(PotionEffect effect, boolean override) {
        int effectId = effect.getType().getId();
        MobEffectInstance existing = null;
        for (MobEffectInstance mobEffect : this.getHandle().effects) {
            if (MobEffect.getId(mobEffect.getEffect()) == effectId) {
                existing = mobEffect;
            }
        }
        if (existing != null) {
            if (!override) {
                return false;
            }
            this.getHandle().effects.remove(existing);
        }
        this.getHandle().addEffect(CraftPotionUtil.fromBukkit(effect));
        this.getHandle().refreshEffects();
        return true;
    }

    @Override
    public void clearCustomEffects() {
        this.getHandle().effects.clear();
        this.getHandle().refreshEffects();
    }

    @Override
    public List<PotionEffect> getCustomEffects() {
        ImmutableList.Builder<PotionEffect> builder = ImmutableList.builder();
        for (MobEffectInstance effect : this.getHandle().effects) {
            builder.add(CraftPotionUtil.toBukkit(effect));
        }
        return builder.build();
    }

    @Override
    public boolean hasCustomEffect(PotionEffectType type) {
        for (MobEffectInstance effect : this.getHandle().effects) {
            if (CraftPotionUtil.equals(effect.getEffect(), type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasCustomEffects() {
        return !this.getHandle().effects.isEmpty();
    }

    @Override
    public boolean removeCustomEffect(PotionEffectType effect) {
        int effectId = effect.getId();
        MobEffectInstance existing = null;
        for (MobEffectInstance mobEffect : this.getHandle().effects) {
            if (MobEffect.getId(mobEffect.getEffect()) == effectId) {
                existing = mobEffect;
            }
        }
        if (existing == null) {
            return false;
        }
        this.getHandle().effects.remove(existing);
        this.getHandle().refreshEffects();
        return true;
    }

    @Override
    public void setBasePotionData(PotionData data) {
        Validate.notNull(data, "PotionData cannot be null");
        this.getHandle().setPotionType(CraftPotionUtil.fromBukkit(data));
    }

    @Override
    public PotionData getBasePotionData() {
        return CraftPotionUtil.toBukkit(this.getHandle().getPotionType());
    }

    @Override
    public ProjectileSource getSource() {
        net.minecraft.world.entity.LivingEntity source = this.getHandle().getOwner();
        return (source == null) ? null : (LivingEntity) source.getBukkitEntity();
    }

    @Override
    public void setSource(ProjectileSource shooter) {
        if (shooter instanceof CraftLivingEntity) {
            this.getHandle().setOwner((net.minecraft.world.entity.LivingEntity) ((CraftLivingEntity) shooter).getHandle());
        } else {
            this.getHandle().setOwner((net.minecraft.world.entity.LivingEntity) null);
        }
    }
}
