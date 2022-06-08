package org.bukkit.craftbukkit.v1_19_R1.entity;

import com.google.common.base.Preconditions;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;

// Paper start
import com.destroystokyo.paper.entity.villager.Reputation;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
// Paper end

public class CraftVillager extends CraftAbstractVillager implements Villager {

    public CraftVillager(CraftServer server, net.minecraft.world.entity.npc.Villager entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.npc.Villager getHandle() {
        return (net.minecraft.world.entity.npc.Villager) entity;
    }

    @Override
    public String toString() {
        return "CraftVillager";
    }

    @Override
    public EntityType getType() {
        return EntityType.VILLAGER;
    }

    @Override
    public void remove() {
        this.getHandle().releaseAllPois();

        super.remove();
    }

    @Override
    public Profession getProfession() {
        return CraftVillager.nmsToBukkitProfession(this.getHandle().getVillagerData().getProfession());
    }

    @Override
    public void setProfession(Profession profession) {
        Validate.notNull(profession);
        this.getHandle().setVillagerData(this.getHandle().getVillagerData().setProfession(CraftVillager.bukkitToNmsProfession(profession)));
    }

    @Override
    public Type getVillagerType() {
        return Type.valueOf(Registry.VILLAGER_TYPE.getKey(this.getHandle().getVillagerData().getType()).getPath().toUpperCase(Locale.ROOT));
    }

    @Override
    public void setVillagerType(Type type) {
        Validate.notNull(type);
        this.getHandle().setVillagerData(this.getHandle().getVillagerData().setType(Registry.VILLAGER_TYPE.get(CraftNamespacedKey.toMinecraft(type.getKey()))));
    }

    @Override
    public int getVillagerLevel() {
        return this.getHandle().getVillagerData().getLevel();
    }

    @Override
    public void setVillagerLevel(int level) {
        Preconditions.checkArgument(1 <= level && level <= 5, "level must be between [1, 5]");

        this.getHandle().setVillagerData(this.getHandle().getVillagerData().setLevel(level));
    }

    @Override
    public int getVillagerExperience() {
        return this.getHandle().getVillagerXp();
    }

    @Override
    public void setVillagerExperience(int experience) {
        Preconditions.checkArgument(experience >= 0, "Experience must be positive");

        this.getHandle().setVillagerXp(experience);
    }

    // Paper start
    @Override
    public int getRestocksToday() {
        return getHandle().numberOfRestocksToday;
    }

    @Override
    public void setRestocksToday(int restocksToday) {
        getHandle().numberOfRestocksToday = restocksToday;
    }
    // Paper end

    @Override
    public boolean sleep(Location location) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "Location needs to be in a world");
        Preconditions.checkArgument(location.getWorld().equals(getWorld()), "Cannot sleep across worlds");
        Preconditions.checkState(!this.getHandle().generation, "Cannot sleep during world generation");

        BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockState iblockdata = this.getHandle().level.getBlockState(position);
        if (!(iblockdata.getBlock() instanceof BedBlock)) {
            return false;
        }

        this.getHandle().startSleeping(position);
        return true;
    }

    @Override
    public void wakeup() {
        Preconditions.checkState(isSleeping(), "Cannot wakeup if not sleeping");
        Preconditions.checkState(!this.getHandle().generation, "Cannot wakeup during world generation");

        this.getHandle().stopSleeping();
    }

    @Override
    public void shakeHead() {
        this.getHandle().setUnhappy();
    }

    @Override
    public ZombieVillager zombify() {
        net.minecraft.world.entity.monster.ZombieVillager entityzombievillager = Zombie.zombifyVillager(this.getHandle().level.getMinecraftWorld(), this.getHandle(), this.getHandle().blockPosition(), isSilent(), CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (entityzombievillager != null) ? (ZombieVillager) entityzombievillager.getBukkitEntity() : null;
    }

    public static Profession nmsToBukkitProfession(VillagerProfession nms) {
        return Profession.valueOf(Registry.VILLAGER_PROFESSION.getKey(nms).getPath().toUpperCase(Locale.ROOT));
    }

    public static VillagerProfession bukkitToNmsProfession(Profession bukkit) {
        return Registry.VILLAGER_PROFESSION.get(CraftNamespacedKey.toMinecraft(bukkit.getKey()));
    }

    // Paper start - Add villager reputation API
    @Override
    public Reputation getReputation(UUID uniqueId) {
        net.minecraft.world.entity.ai.gossip.GossipContainer.EntityGossips rep = getHandle().getGossips().getReputations().get(uniqueId);
        if (rep == null) {
            return new Reputation(Maps.newHashMap());
        }

        return rep.getPaperReputation();
    }

    @Override
    public Map<UUID, Reputation> getReputations() {
        return getHandle().getGossips().getReputations().entrySet()
            .stream()
            .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getPaperReputation()));
    }

    @Override
    public void setReputation(UUID uniqueId, Reputation reputation) {
        net.minecraft.world.entity.ai.gossip.GossipContainer.EntityGossips nmsReputation =
            getHandle().getGossips().getReputations().computeIfAbsent(
                uniqueId,
                key -> new net.minecraft.world.entity.ai.gossip.GossipContainer.EntityGossips()
            );
        nmsReputation.assignFromPaperReputation(reputation);
    }

    @Override
    public void setReputations(Map<UUID, Reputation> reputations) {
        for (Map.Entry<UUID, Reputation> entry : reputations.entrySet()) {
            setReputation(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clearReputations() {
        getHandle().getGossips().getReputations().clear();
    }
    // Paper end
}
