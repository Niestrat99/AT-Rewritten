package org.spigotmc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.ItemEntity;

public class TrackingRange
{

    /**
     * Gets the range an entity should be 'tracked' by players and visible in
     * the client.
     *
     * @param entity
     * @param defaultRange Default range defined by Mojang
     * @return
     */
    public static int getEntityTrackingRange(Entity entity, int defaultRange)
    {
        if ( defaultRange == 0 )
        {
            return defaultRange;
        }
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) return defaultRange; // Paper - enderdragon is exempt
        SpigotWorldConfig config = entity.level.spigotConfig;
        if ( entity instanceof ServerPlayer )
        {
            return config.playerTrackingRange;
        // Paper start - Simplify and set water mobs to animal tracking range
        }
        switch (entity.activationType) {
            case RAIDER:
            case MONSTER:
            case FLYING_MONSTER:
                return config.monsterTrackingRange;
            case WATER:
            case VILLAGER:
            case ANIMAL:
                return config.animalTrackingRange;
            case MISC:
        }
        if ( entity instanceof ItemFrame || entity instanceof Painting || entity instanceof ItemEntity || entity instanceof ExperienceOrb )
        // Paper end
        {
            return config.miscTrackingRange;
        } else
        {
            return config.otherTrackingRange;
        }
    }

    // Paper start - optimise entity tracking
    // copied from above, TODO check on update
    public static TrackingRangeType getTrackingRangeType(Entity entity)
    {
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) return TrackingRangeType.ENDERDRAGON; // Paper - enderdragon is exempt
        if ( entity instanceof ServerPlayer )
        {
            return TrackingRangeType.PLAYER;
            // Paper start - Simplify and set water mobs to animal tracking range
        }
        switch (entity.activationType) {
            case RAIDER:
            case MONSTER:
            case FLYING_MONSTER:
                return TrackingRangeType.MONSTER;
            case WATER:
            case VILLAGER:
            case ANIMAL:
                return TrackingRangeType.ANIMAL;
            case MISC:
        }
        if ( entity instanceof ItemFrame || entity instanceof Painting || entity instanceof ItemEntity || entity instanceof ExperienceOrb )
        // Paper end
        {
            return TrackingRangeType.MISC;
        } else
        {
            return TrackingRangeType.OTHER;
        }
    }

    public static enum TrackingRangeType {
        PLAYER,
        ANIMAL,
        MONSTER,
        MISC,
        OTHER,
        ENDERDRAGON;
    }
    // Paper end - optimise entity tracking
}
