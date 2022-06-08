package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

public class CraftVillagerZombie extends CraftZombie implements ZombieVillager {

    public CraftVillagerZombie(CraftServer server, net.minecraft.world.entity.monster.ZombieVillager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.ZombieVillager getHandle() {
        return (net.minecraft.world.entity.monster.ZombieVillager) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftVillagerZombie";
    }

    @Override
    public EntityType getType() {
        return EntityType.ZOMBIE_VILLAGER;
    }

    @Override
    public Villager.Profession getVillagerProfession() {
        return Villager.Profession.valueOf(Registry.VILLAGER_PROFESSION.getKey(this.getHandle().getVillagerData().getProfession()).getPath().toUpperCase(Locale.ROOT));
    }

    @Override
    public void setVillagerProfession(Villager.Profession profession) {
        Validate.notNull(profession);
        this.getHandle().setVillagerData(this.getHandle().getVillagerData().setProfession(Registry.VILLAGER_PROFESSION.get(new ResourceLocation(profession.name().toLowerCase(Locale.ROOT)))));
    }

    @Override
    public Villager.Type getVillagerType() {
        return Villager.Type.valueOf(Registry.VILLAGER_TYPE.getKey(this.getHandle().getVillagerData().getType()).getPath().toUpperCase(Locale.ROOT));
    }

    @Override
    public void setVillagerType(Villager.Type type) {
        Validate.notNull(type);
        this.getHandle().setVillagerData(this.getHandle().getVillagerData().setType(Registry.VILLAGER_TYPE.get(CraftNamespacedKey.toMinecraft(type.getKey()))));
    }

    @Override
    public boolean isConverting() {
        return this.getHandle().isConverting();
    }

    @Override
    public int getConversionTime() {
        Preconditions.checkState(this.isConverting(), "Entity not converting");

        return this.getHandle().villagerConversionTime;
    }

    @Override
    public void setConversionTime(int time) {
    // Paper start - missing entity behaviour api - converting without entity event
        this.setConversionTime(time, true);
    }

    @Override
    public void setConversionTime(int time, boolean broadcastEntityEvent) {
    // Paper stop - missing entity behaviour api - converting without entity event
        if (time < 0) {
            this.getHandle().villagerConversionTime = -1;
            this.getHandle().getEntityData().set(net.minecraft.world.entity.monster.ZombieVillager.DATA_CONVERTING_ID, false);
            this.getHandle().conversionStarter = null;
            this.getHandle().removeEffect(MobEffects.DAMAGE_BOOST, org.bukkit.event.entity.EntityPotionEffectEvent.Cause.CONVERSION);
        } else {
            this.getHandle().startConverting((UUID) null, time, broadcastEntityEvent); // Paper - missing entity behaviour api - converting without entity event
        }
    }

    @Override
    public OfflinePlayer getConversionPlayer() {
        return (this.getHandle().conversionStarter == null) ? null : Bukkit.getOfflinePlayer(this.getHandle().conversionStarter);
    }

    @Override
    public void setConversionPlayer(OfflinePlayer conversionPlayer) {
        if (!this.isConverting()) return;
        this.getHandle().conversionStarter = (conversionPlayer == null) ? null : conversionPlayer.getUniqueId();
    }
}
