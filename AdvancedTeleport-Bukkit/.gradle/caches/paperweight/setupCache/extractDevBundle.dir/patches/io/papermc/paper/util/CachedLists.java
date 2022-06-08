package io.papermc.paper.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.util.UnsafeList;
import java.util.List;

public final class CachedLists {

    // Paper start - optimise collisions
    static final UnsafeList<AABB> TEMP_COLLISION_LIST = new UnsafeList<>(1024);
    static boolean tempCollisionListInUse;

    public static UnsafeList<AABB> getTempCollisionList() {
        if (!Bukkit.isPrimaryThread() || tempCollisionListInUse) {
            return new UnsafeList<>(16);
        }
        tempCollisionListInUse = true;
        return TEMP_COLLISION_LIST;
    }

    public static void returnTempCollisionList(List<AABB> list) {
        if (list != TEMP_COLLISION_LIST) {
            return;
        }
        ((UnsafeList)list).setSize(0);
        tempCollisionListInUse = false;
    }

    static final UnsafeList<Entity> TEMP_GET_ENTITIES_LIST = new UnsafeList<>(1024);
    static boolean tempGetEntitiesListInUse;

    public static UnsafeList<Entity> getTempGetEntitiesList() {
        if (!Bukkit.isPrimaryThread() || tempGetEntitiesListInUse) {
            return new UnsafeList<>(16);
        }
        tempGetEntitiesListInUse = true;
        return TEMP_GET_ENTITIES_LIST;
    }

    public static void returnTempGetEntitiesList(List<Entity> list) {
        if (list != TEMP_GET_ENTITIES_LIST) {
            return;
        }
        ((UnsafeList)list).setSize(0);
        tempGetEntitiesListInUse = false;
    }
    // Paper end - optimise collisions

    public static void reset() {
        // Paper start - optimise collisions
        TEMP_COLLISION_LIST.completeReset();
        TEMP_GET_ENTITIES_LIST.completeReset();
        // Paper end - optimise collisions
    }
}
