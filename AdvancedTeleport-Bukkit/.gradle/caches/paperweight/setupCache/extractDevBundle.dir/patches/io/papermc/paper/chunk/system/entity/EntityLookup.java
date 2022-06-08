package io.papermc.paper.chunk.system.entity;

import com.destroystokyo.paper.util.maplist.EntityList;
import com.mojang.logging.LogUtils;
import io.papermc.paper.util.CoordinateUtils;
import io.papermc.paper.util.TickThread;
import io.papermc.paper.util.WorldUtil;
import io.papermc.paper.world.ChunkEntitySlices;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.BlockPos;
import io.papermc.paper.chunk.system.ChunkSystem;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EntityLookup implements LevelEntityGetter<Entity> {

    private static final Logger LOGGER = LogUtils.getClassLogger();

    protected static final int REGION_SHIFT = 5;
    protected static final int REGION_MASK = (1 << REGION_SHIFT) - 1;
    protected static final int REGION_SIZE = 1 << REGION_SHIFT;

    public final ServerLevel world;

    private final StampedLock stateLock = new StampedLock();
    protected final Long2ObjectOpenHashMap<ChunkSlicesRegion> regions = new Long2ObjectOpenHashMap<>(128, 0.5f);

    private final int minSection; // inclusive
    private final int maxSection; // inclusive
    private final LevelCallback<Entity> worldCallback;

    private final StampedLock entityByLock = new StampedLock();
    private final Int2ReferenceOpenHashMap<Entity> entityById = new Int2ReferenceOpenHashMap<>();
    private final Object2ReferenceOpenHashMap<UUID, Entity> entityByUUID = new Object2ReferenceOpenHashMap<>();
    private final EntityList accessibleEntities = new EntityList();

    public EntityLookup(final ServerLevel world, final LevelCallback<Entity> worldCallback) {
        this.world = world;
        this.minSection = WorldUtil.getMinSection(world);
        this.maxSection = WorldUtil.getMaxSection(world);
        this.worldCallback = worldCallback;
    }

    private static Entity maskNonAccessible(final Entity entity) {
        if (entity == null) {
            return null;
        }
        final Visibility visibility = EntityLookup.getEntityStatus(entity);
        return visibility.isAccessible() ? entity : null;
    }

    @Nullable
    @Override
    public Entity get(final int id) {
        final long attempt = this.entityByLock.tryOptimisticRead();
        if (attempt != 0L) {
            try {
                final Entity ret = this.entityById.get(id);

                if (this.entityByLock.validate(attempt)) {
                    return maskNonAccessible(ret);
                }
            } catch (final Error error) {
                throw error;
            } catch (final Throwable thr) {
                // ignore
            }
        }

        this.entityByLock.readLock();
        try {
            return maskNonAccessible(this.entityById.get(id));
        } finally {
            this.entityByLock.tryUnlockRead();
        }
    }

    @Nullable
    @Override
    public Entity get(final UUID id) {
        final long attempt = this.entityByLock.tryOptimisticRead();
        if (attempt != 0L) {
            try {
                final Entity ret = this.entityByUUID.get(id);

                if (this.entityByLock.validate(attempt)) {
                    return maskNonAccessible(ret);
                }
            } catch (final Error error) {
                throw error;
            } catch (final Throwable thr) {
                // ignore
            }
        }

        this.entityByLock.readLock();
        try {
            return maskNonAccessible(this.entityByUUID.get(id));
        } finally {
            this.entityByLock.tryUnlockRead();
        }
    }

    public boolean hasEntity(final UUID uuid) {
        return this.get(uuid) != null;
    }

    public String getDebugInfo() {
        return "count_id:" + this.entityById.size() + ",count_uuid:" + this.entityByUUID.size() + ",region_count:" + this.regions.size();
    }

    static final class ArrayIterable<T> implements Iterable<T> {

        private final T[] array;
        private final int off;
        private final int length;

        public ArrayIterable(final T[] array, final int off, final int length) {
            this.array = array;
            this.off = off;
            this.length = length;
            if (length > array.length) {
                throw new IllegalArgumentException("Length must be no greater-than the array length");
            }
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return new ArrayIterator<>(this.array, this.off, this.length);
        }

        static final class ArrayIterator<T> implements Iterator<T> {

            private final T[] array;
            private int off;
            private final int length;

            public ArrayIterator(final T[] array, final int off, final int length) {
                this.array = array;
                this.off = off;
                this.length = length;
            }

            @Override
            public boolean hasNext() {
                return this.off < this.length;
            }

            @Override
            public T next() {
                if (this.off >= this.length) {
                    throw new NoSuchElementException();
                }
                return this.array[this.off++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public Iterable<Entity> getAll() {
        return new ArrayIterable<>(this.accessibleEntities.getRawData(), 0, this.accessibleEntities.size());
    }

    @Override
    public <U extends Entity> void get(final EntityTypeTest<Entity, U> filter, final Consumer<U> action) {
        for (final Entity entity : this.entityById.values()) {
            final Visibility visibility = EntityLookup.getEntityStatus(entity);
            if (!visibility.isAccessible()) {
                continue;
            }
            final U casted = filter.tryCast(entity);
            if (casted != null) {
                action.accept(casted);
            }
        }
    }

    @Override
    public void get(final AABB box, final Consumer<Entity> action) {
        List<Entity> entities = new ArrayList<>();
        this.getEntitiesWithoutDragonParts(null, box, entities, null);
        for (int i = 0, len = entities.size(); i < len; ++i) {
            action.accept(entities.get(i));
        }
    }

    @Override
    public <U extends Entity> void get(final EntityTypeTest<Entity, U> filter, final AABB box, final Consumer<U> action) {
        List<Entity> entities = new ArrayList<>();
        this.getEntitiesWithoutDragonParts(null, box, entities, null);
        for (int i = 0, len = entities.size(); i < len; ++i) {
            final U casted = filter.tryCast(entities.get(i));
            if (casted != null) {
                action.accept(casted);
            }
        }
    }

    public void entityStatusChange(final Entity entity, final ChunkEntitySlices slices, final Visibility oldVisibility, final Visibility newVisibility, final boolean moved,
                                   final boolean created, final boolean destroyed) {
        TickThread.ensureTickThread(entity, "Entity status change must only happen on the main thread");

        if (entity.updatingSectionStatus) {
            // recursive status update
            LOGGER.error("Cannot recursively update entity chunk status for entity " + entity, new Throwable());
            return;
        }

        final boolean entityStatusUpdateBefore = slices == null ? false : slices.startPreventingStatusUpdates();

        if (entityStatusUpdateBefore) {
            LOGGER.error("Cannot update chunk status for entity " + entity + " since entity chunk (" + slices.chunkX + "," + slices.chunkZ + ") is receiving update", new Throwable());
            return;
        }

        try {
            final Boolean ticketBlockBefore = this.world.chunkTaskScheduler.chunkHolderManager.blockTicketUpdates();
            try {
                entity.updatingSectionStatus = true;
                try {
                    if (created) {
                        EntityLookup.this.worldCallback.onCreated(entity);
                    }

                    if (oldVisibility == newVisibility) {
                        if (moved && newVisibility.isAccessible()) {
                            EntityLookup.this.worldCallback.onSectionChange(entity);
                        }
                        return;
                    }

                    if (newVisibility.ordinal() > oldVisibility.ordinal()) {
                        // status upgrade
                        if (!oldVisibility.isAccessible() && newVisibility.isAccessible()) {
                            this.accessibleEntities.add(entity);
                            EntityLookup.this.worldCallback.onTrackingStart(entity);
                        }

                        if (!oldVisibility.isTicking() && newVisibility.isTicking()) {
                            EntityLookup.this.worldCallback.onTickingStart(entity);
                        }
                    } else {
                        // status downgrade
                        if (oldVisibility.isTicking() && !newVisibility.isTicking()) {
                            EntityLookup.this.worldCallback.onTickingEnd(entity);
                        }

                        if (oldVisibility.isAccessible() && !newVisibility.isAccessible()) {
                            this.accessibleEntities.remove(entity);
                            EntityLookup.this.worldCallback.onTrackingEnd(entity);
                        }
                    }

                    if (moved && newVisibility.isAccessible()) {
                        EntityLookup.this.worldCallback.onSectionChange(entity);
                    }

                    if (destroyed) {
                        EntityLookup.this.worldCallback.onDestroyed(entity);
                    }
                } finally {
                    entity.updatingSectionStatus = false;
                }
            } finally {
                this.world.chunkTaskScheduler.chunkHolderManager.unblockTicketUpdates(ticketBlockBefore);
            }
        } finally {
            if (slices != null) {
                slices.stopPreventingStatusUpdates(false);
            }
        }
    }

    public void chunkStatusChange(final int x, final int z, final ChunkHolder.FullChunkStatus newStatus) {
        this.getChunk(x, z).updateStatus(newStatus, this);
    }

    public void addLegacyChunkEntities(final List<Entity> entities) {
        for (int i = 0, len = entities.size(); i < len; ++i) {
            this.addEntity(entities.get(i), true);
        }
    }

    public void addEntityChunkEntities(final List<Entity> entities) {
        for (int i = 0, len = entities.size(); i < len; ++i) {
            this.addEntity(entities.get(i), true);
        }
    }

    public void addWorldGenChunkEntities(final List<Entity> entities) {
        for (int i = 0, len = entities.size(); i < len; ++i) {
            this.addEntity(entities.get(i), false);
        }
    }

    public boolean addNewEntity(final Entity entity) {
        return this.addEntity(entity, false);
    }

    public static Visibility getEntityStatus(final Entity entity) {
        if (entity.isAlwaysTicking()) {
            return Visibility.TICKING;
        }
        final ChunkHolder.FullChunkStatus entityStatus = entity.chunkStatus;
        return Visibility.fromFullChunkStatus(entityStatus == null ? ChunkHolder.FullChunkStatus.INACCESSIBLE : entityStatus);
    }

    private boolean addEntity(final Entity entity, final boolean fromDisk) {
        final BlockPos pos = entity.blockPosition();
        final int sectionX = pos.getX() >> 4;
        final int sectionY = Mth.clamp(pos.getY() >> 4, this.minSection, this.maxSection);
        final int sectionZ = pos.getZ() >> 4;
        TickThread.ensureTickThread(this.world, sectionX, sectionZ, "Cannot add entity off-main thread");

        if (entity.isRemoved()) {
            LOGGER.warn("Refusing to add removed entity: " + entity);
            return false;
        }

        if (entity.updatingSectionStatus) {
            LOGGER.warn("Entity " + entity + " is currently prevented from being added/removed to world since it is processing section status updates", new Throwable());
            return false;
        }

        if (fromDisk) {
            ChunkSystem.onEntityPreAdd(this.world, entity);
            if (entity.isRemoved()) {
                // removed from checkDupeUUID call
                return false;
            }
        }

        this.entityByLock.writeLock();
        try {
            if (this.entityById.containsKey(entity.getId())) {
                LOGGER.warn("Entity id already exists: " + entity.getId() + ", mapped to " + this.entityById.get(entity.getId()) + ", can't add " + entity);
                return false;
            }
            if (this.entityByUUID.containsKey(entity.getUUID())) {
                LOGGER.warn("Entity uuid already exists: " + entity.getUUID() + ", mapped to " + this.entityByUUID.get(entity.getUUID()) + ", can't add " + entity);
                return false;
            }
            this.entityById.put(entity.getId(), entity);
            this.entityByUUID.put(entity.getUUID(), entity);
        } finally {
            this.entityByLock.tryUnlockWrite();
        }

        entity.sectionX = sectionX;
        entity.sectionY = sectionY;
        entity.sectionZ = sectionZ;
        final ChunkEntitySlices slices = this.getOrCreateChunk(sectionX, sectionZ);
        if (!slices.addEntity(entity, sectionY)) {
            LOGGER.warn("Entity " + entity + " added to world '" + this.world.getWorld().getName() + "', but was already contained in entity chunk (" + sectionX + "," + sectionZ + ")");
        }

        entity.setLevelCallback(new EntityCallback(entity));

        this.entityStatusChange(entity, slices, Visibility.HIDDEN, getEntityStatus(entity), false, !fromDisk, false);

        return true;
    }

    private void removeEntity(final Entity entity) {
        final int sectionX = entity.sectionX;
        final int sectionY = entity.sectionY;
        final int sectionZ = entity.sectionZ;
        TickThread.ensureTickThread(this.world, sectionX, sectionZ, "Cannot remove entity off-main");
        if (!entity.isRemoved()) {
            throw new IllegalStateException("Only call Entity#setRemoved to remove an entity");
        }
        final ChunkEntitySlices slices = this.getChunk(sectionX, sectionZ);
        // all entities should be in a chunk
        if (slices == null) {
            LOGGER.warn("Cannot remove entity " + entity + " from null entity slices (" + sectionX + "," + sectionZ + ")");
        } else {
            if (!slices.removeEntity(entity, sectionY)) {
                LOGGER.warn("Failed to remove entity " + entity + " from entity slices (" + sectionX + "," + sectionZ + ")");
            }
        }
        entity.sectionX = entity.sectionY = entity.sectionZ = Integer.MIN_VALUE;

        this.entityByLock.writeLock();
        try {
            if (!this.entityById.remove(entity.getId(), entity)) {
                LOGGER.warn("Failed to remove entity " + entity + " by id, current entity mapped: " + this.entityById.get(entity.getId()));
            }
            if (!this.entityByUUID.remove(entity.getUUID(), entity)) {
                LOGGER.warn("Failed to remove entity " + entity + " by uuid, current entity mapped: " + this.entityByUUID.get(entity.getUUID()));
            }
        } finally {
            this.entityByLock.tryUnlockWrite();
        }
    }

    private ChunkEntitySlices moveEntity(final Entity entity) {
        // ensure we own the entity
        TickThread.ensureTickThread(entity, "Cannot move entity off-main");

        final BlockPos newPos = entity.blockPosition();
        final int newSectionX = newPos.getX() >> 4;
        final int newSectionY = Mth.clamp(newPos.getY() >> 4, this.minSection, this.maxSection);
        final int newSectionZ = newPos.getZ() >> 4;

        if (newSectionX == entity.sectionX && newSectionY == entity.sectionY && newSectionZ == entity.sectionZ) {
            return null;
        }

        // ensure the new section is owned by this tick thread
        TickThread.ensureTickThread(this.world, newSectionX, newSectionZ, "Cannot move entity off-main");

        // ensure the old section is owned by this tick thread
        TickThread.ensureTickThread(this.world, entity.sectionX, entity.sectionZ, "Cannot move entity off-main");

        final ChunkEntitySlices old = this.getChunk(entity.sectionX, entity.sectionZ);
        final ChunkEntitySlices slices = this.getOrCreateChunk(newSectionX, newSectionZ);

        if (!old.removeEntity(entity, entity.sectionY)) {
            LOGGER.warn("Could not remove entity " + entity + " from its old chunk section (" + entity.sectionX + "," + entity.sectionY + "," + entity.sectionZ + ") since it was not contained in the section");
        }

        if (!slices.addEntity(entity, newSectionY)) {
            LOGGER.warn("Could not add entity " + entity + " to its new chunk section (" + newSectionX + "," + newSectionY + "," + newSectionZ + ") as it is already contained in the section");
        }

        entity.sectionX = newSectionX;
        entity.sectionY = newSectionY;
        entity.sectionZ = newSectionZ;

        return slices;
    }

    public void getEntitiesWithoutDragonParts(final Entity except, final AABB box, final List<Entity> into, final Predicate<? super Entity> predicate) {
        final int minChunkX = (Mth.floor(box.minX) - 2) >> 4;
        final int minChunkZ = (Mth.floor(box.minZ) - 2) >> 4;
        final int maxChunkX = (Mth.floor(box.maxX) + 2) >> 4;
        final int maxChunkZ = (Mth.floor(box.maxZ) + 2) >> 4;

        final int minRegionX = minChunkX >> REGION_SHIFT;
        final int minRegionZ = minChunkZ >> REGION_SHIFT;
        final int maxRegionX = maxChunkX >> REGION_SHIFT;
        final int maxRegionZ = maxChunkZ >> REGION_SHIFT;

        for (int currRegionZ = minRegionZ; currRegionZ <= maxRegionZ; ++currRegionZ) {
            final int minZ = currRegionZ == minRegionZ ? minChunkZ & REGION_MASK : 0;
            final int maxZ = currRegionZ == maxRegionZ ? maxChunkZ & REGION_MASK : REGION_MASK;

            for (int currRegionX = minRegionX; currRegionX <= maxRegionX; ++currRegionX) {
                final ChunkSlicesRegion region = this.getRegion(currRegionX, currRegionZ);

                if (region == null) {
                    continue;
                }

                final int minX = currRegionX == minRegionX ? minChunkX & REGION_MASK : 0;
                final int maxX = currRegionX == maxRegionX ? maxChunkX & REGION_MASK : REGION_MASK;

                for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                    for (int currX = minX; currX <= maxX; ++currX) {
                        final ChunkEntitySlices chunk = region.get(currX | (currZ << REGION_SHIFT));
                        if (chunk == null || !chunk.status.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                            continue;
                        }

                        chunk.getEntitiesWithoutDragonParts(except, box, into, predicate);
                    }
                }
            }
        }
    }

    public void getEntities(final Entity except, final AABB box, final List<Entity> into, final Predicate<? super Entity> predicate) {
        final int minChunkX = (Mth.floor(box.minX) - 2) >> 4;
        final int minChunkZ = (Mth.floor(box.minZ) - 2) >> 4;
        final int maxChunkX = (Mth.floor(box.maxX) + 2) >> 4;
        final int maxChunkZ = (Mth.floor(box.maxZ) + 2) >> 4;

        final int minRegionX = minChunkX >> REGION_SHIFT;
        final int minRegionZ = minChunkZ >> REGION_SHIFT;
        final int maxRegionX = maxChunkX >> REGION_SHIFT;
        final int maxRegionZ = maxChunkZ >> REGION_SHIFT;

        for (int currRegionZ = minRegionZ; currRegionZ <= maxRegionZ; ++currRegionZ) {
            final int minZ = currRegionZ == minRegionZ ? minChunkZ & REGION_MASK : 0;
            final int maxZ = currRegionZ == maxRegionZ ? maxChunkZ & REGION_MASK : REGION_MASK;

            for (int currRegionX = minRegionX; currRegionX <= maxRegionX; ++currRegionX) {
                final ChunkSlicesRegion region = this.getRegion(currRegionX, currRegionZ);

                if (region == null) {
                    continue;
                }

                final int minX = currRegionX == minRegionX ? minChunkX & REGION_MASK : 0;
                final int maxX = currRegionX == maxRegionX ? maxChunkX & REGION_MASK : REGION_MASK;

                for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                    for (int currX = minX; currX <= maxX; ++currX) {
                        final ChunkEntitySlices chunk = region.get(currX | (currZ << REGION_SHIFT));
                        if (chunk == null || !chunk.status.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                            continue;
                        }

                        chunk.getEntities(except, box, into, predicate);
                    }
                }
            }
        }
    }

    public void getHardCollidingEntities(final Entity except, final AABB box, final List<Entity> into, final Predicate<? super Entity> predicate) {
        final int minChunkX = (Mth.floor(box.minX) - 2) >> 4;
        final int minChunkZ = (Mth.floor(box.minZ) - 2) >> 4;
        final int maxChunkX = (Mth.floor(box.maxX) + 2) >> 4;
        final int maxChunkZ = (Mth.floor(box.maxZ) + 2) >> 4;

        final int minRegionX = minChunkX >> REGION_SHIFT;
        final int minRegionZ = minChunkZ >> REGION_SHIFT;
        final int maxRegionX = maxChunkX >> REGION_SHIFT;
        final int maxRegionZ = maxChunkZ >> REGION_SHIFT;

        for (int currRegionZ = minRegionZ; currRegionZ <= maxRegionZ; ++currRegionZ) {
            final int minZ = currRegionZ == minRegionZ ? minChunkZ & REGION_MASK : 0;
            final int maxZ = currRegionZ == maxRegionZ ? maxChunkZ & REGION_MASK : REGION_MASK;

            for (int currRegionX = minRegionX; currRegionX <= maxRegionX; ++currRegionX) {
                final ChunkSlicesRegion region = this.getRegion(currRegionX, currRegionZ);

                if (region == null) {
                    continue;
                }

                final int minX = currRegionX == minRegionX ? minChunkX & REGION_MASK : 0;
                final int maxX = currRegionX == maxRegionX ? maxChunkX & REGION_MASK : REGION_MASK;

                for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                    for (int currX = minX; currX <= maxX; ++currX) {
                        final ChunkEntitySlices chunk = region.get(currX | (currZ << REGION_SHIFT));
                        if (chunk == null || !chunk.status.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                            continue;
                        }

                        chunk.getHardCollidingEntities(except, box, into, predicate);
                    }
                }
            }
        }
    }

    public <T extends Entity> void getEntities(final EntityType<?> type, final AABB box, final List<? super T> into,
                                               final Predicate<? super T> predicate) {
        final int minChunkX = (Mth.floor(box.minX) - 2) >> 4;
        final int minChunkZ = (Mth.floor(box.minZ) - 2) >> 4;
        final int maxChunkX = (Mth.floor(box.maxX) + 2) >> 4;
        final int maxChunkZ = (Mth.floor(box.maxZ) + 2) >> 4;

        final int minRegionX = minChunkX >> REGION_SHIFT;
        final int minRegionZ = minChunkZ >> REGION_SHIFT;
        final int maxRegionX = maxChunkX >> REGION_SHIFT;
        final int maxRegionZ = maxChunkZ >> REGION_SHIFT;

        for (int currRegionZ = minRegionZ; currRegionZ <= maxRegionZ; ++currRegionZ) {
            final int minZ = currRegionZ == minRegionZ ? minChunkZ & REGION_MASK : 0;
            final int maxZ = currRegionZ == maxRegionZ ? maxChunkZ & REGION_MASK : REGION_MASK;

            for (int currRegionX = minRegionX; currRegionX <= maxRegionX; ++currRegionX) {
                final ChunkSlicesRegion region = this.getRegion(currRegionX, currRegionZ);

                if (region == null) {
                    continue;
                }

                final int minX = currRegionX == minRegionX ? minChunkX & REGION_MASK : 0;
                final int maxX = currRegionX == maxRegionX ? maxChunkX & REGION_MASK : REGION_MASK;

                for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                    for (int currX = minX; currX <= maxX; ++currX) {
                        final ChunkEntitySlices chunk = region.get(currX | (currZ << REGION_SHIFT));
                        if (chunk == null || !chunk.status.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                            continue;
                        }

                        chunk.getEntities(type, box, (List)into, (Predicate)predicate);
                    }
                }
            }
        }
    }

    public <T extends Entity> void getEntities(final Class<? extends T> clazz, final Entity except, final AABB box, final List<? super T> into,
                                               final Predicate<? super T> predicate) {
        final int minChunkX = (Mth.floor(box.minX) - 2) >> 4;
        final int minChunkZ = (Mth.floor(box.minZ) - 2) >> 4;
        final int maxChunkX = (Mth.floor(box.maxX) + 2) >> 4;
        final int maxChunkZ = (Mth.floor(box.maxZ) + 2) >> 4;

        final int minRegionX = minChunkX >> REGION_SHIFT;
        final int minRegionZ = minChunkZ >> REGION_SHIFT;
        final int maxRegionX = maxChunkX >> REGION_SHIFT;
        final int maxRegionZ = maxChunkZ >> REGION_SHIFT;

        for (int currRegionZ = minRegionZ; currRegionZ <= maxRegionZ; ++currRegionZ) {
            final int minZ = currRegionZ == minRegionZ ? minChunkZ & REGION_MASK : 0;
            final int maxZ = currRegionZ == maxRegionZ ? maxChunkZ & REGION_MASK : REGION_MASK;

            for (int currRegionX = minRegionX; currRegionX <= maxRegionX; ++currRegionX) {
                final ChunkSlicesRegion region = this.getRegion(currRegionX, currRegionZ);

                if (region == null) {
                    continue;
                }

                final int minX = currRegionX == minRegionX ? minChunkX & REGION_MASK : 0;
                final int maxX = currRegionX == maxRegionX ? maxChunkX & REGION_MASK : REGION_MASK;

                for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                    for (int currX = minX; currX <= maxX; ++currX) {
                        final ChunkEntitySlices chunk = region.get(currX | (currZ << REGION_SHIFT));
                        if (chunk == null || !chunk.status.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                            continue;
                        }

                        chunk.getEntities(clazz, except, box, into, predicate);
                    }
                }
            }
        }
    }

    public void entitySectionLoad(final int chunkX, final int chunkZ, final ChunkEntitySlices slices) {
        TickThread.ensureTickThread(this.world, chunkX, chunkZ, "Cannot load in entity section off-main");
        synchronized (this) {
            final ChunkEntitySlices curr = this.getChunk(chunkX, chunkZ);
            if (curr != null) {
                this.removeChunk(chunkX, chunkZ);

                curr.mergeInto(slices);

                this.addChunk(chunkX, chunkZ, slices);
            } else {
                this.addChunk(chunkX, chunkZ, slices);
            }
        }
    }

    public void entitySectionUnload(final int chunkX, final int chunkZ) {
        TickThread.ensureTickThread(this.world, chunkX, chunkZ, "Cannot unload entity section off-main");
        this.removeChunk(chunkX, chunkZ);
    }

    public ChunkEntitySlices getChunk(final int chunkX, final int chunkZ) {
        final ChunkSlicesRegion region = this.getRegion(chunkX >> REGION_SHIFT, chunkZ >> REGION_SHIFT);
        if (region == null) {
            return null;
        }

        return region.get((chunkX & REGION_MASK) | ((chunkZ & REGION_MASK) << REGION_SHIFT));
    }

    public ChunkEntitySlices getOrCreateChunk(final int chunkX, final int chunkZ) {
        final ChunkSlicesRegion region = this.getRegion(chunkX >> REGION_SHIFT, chunkZ >> REGION_SHIFT);
        ChunkEntitySlices ret;
        if (region == null || (ret = region.get((chunkX & REGION_MASK) | ((chunkZ & REGION_MASK) << REGION_SHIFT))) == null) {
            // loadInEntityChunk will call addChunk for us
            return this.world.chunkTaskScheduler.chunkHolderManager.getOrCreateEntityChunk(chunkX, chunkZ, true);
        }

        return ret;
    }

    public ChunkSlicesRegion getRegion(final int regionX, final int regionZ) {
        final long key = CoordinateUtils.getChunkKey(regionX, regionZ);
        final long attempt = this.stateLock.tryOptimisticRead();
        if (attempt != 0L) {
            try {
                final ChunkSlicesRegion ret = this.regions.get(key);

                if (this.stateLock.validate(attempt)) {
                    return ret;
                }
            } catch (final Error error) {
                throw error;
            } catch (final Throwable thr) {
                // ignore
            }
        }

        this.stateLock.readLock();
        try {
            return this.regions.get(key);
        } finally {
            this.stateLock.tryUnlockRead();
        }
    }

    private synchronized void removeChunk(final int chunkX, final int chunkZ) {
        final long key = CoordinateUtils.getChunkKey(chunkX >> REGION_SHIFT, chunkZ >> REGION_SHIFT);
        final int relIndex = (chunkX & REGION_MASK) | ((chunkZ & REGION_MASK) << REGION_SHIFT);

        final ChunkSlicesRegion region = this.regions.get(key);
        final int remaining = region.remove(relIndex);

        if (remaining == 0) {
            this.stateLock.writeLock();
            try {
                this.regions.remove(key);
            } finally {
                this.stateLock.tryUnlockWrite();
            }
        }
    }

    public synchronized void addChunk(final int chunkX, final int chunkZ, final ChunkEntitySlices slices) {
        final long key = CoordinateUtils.getChunkKey(chunkX >> REGION_SHIFT, chunkZ >> REGION_SHIFT);
        final int relIndex = (chunkX & REGION_MASK) | ((chunkZ & REGION_MASK) << REGION_SHIFT);

        ChunkSlicesRegion region = this.regions.get(key);
        if (region != null) {
            region.add(relIndex, slices);
        } else {
            region = new ChunkSlicesRegion();
            region.add(relIndex, slices);
            this.stateLock.writeLock();
            try {
                this.regions.put(key, region);
            } finally {
                this.stateLock.tryUnlockWrite();
            }
        }
    }

    public static final class ChunkSlicesRegion {

        protected final ChunkEntitySlices[] slices = new ChunkEntitySlices[REGION_SIZE * REGION_SIZE];
        protected int sliceCount;

        public ChunkEntitySlices get(final int index) {
            return this.slices[index];
        }

        public int remove(final int index) {
            final ChunkEntitySlices slices = this.slices[index];
            if (slices == null) {
                throw new IllegalStateException();
            }

            this.slices[index] = null;

            return --this.sliceCount;
        }

        public void add(final int index, final ChunkEntitySlices slices) {
            final ChunkEntitySlices curr = this.slices[index];
            if (curr != null) {
                throw new IllegalStateException();
            }

            this.slices[index] = slices;

            ++this.sliceCount;
        }
    }

    private final class EntityCallback implements EntityInLevelCallback {

        public final Entity entity;

        public EntityCallback(final Entity entity) {
            this.entity = entity;
        }

        @Override
        public void onMove() {
            final Entity entity = this.entity;
            final Visibility oldVisibility = getEntityStatus(entity);
            final ChunkEntitySlices newSlices = EntityLookup.this.moveEntity(this.entity);
            if (newSlices == null) {
                // no new section, so didn't change sections
                return;
            }
            final Visibility newVisibility = getEntityStatus(entity);

            EntityLookup.this.entityStatusChange(entity, newSlices, oldVisibility, newVisibility, true, false, false);
        }

        @Override
        public void onRemove(final Entity.RemovalReason reason) {
            final Entity entity = this.entity;
            TickThread.ensureTickThread(entity, "Cannot remove entity off-main"); // Paper - rewrite chunk system
            final Visibility tickingState = EntityLookup.getEntityStatus(entity);

            EntityLookup.this.removeEntity(entity);

            EntityLookup.this.entityStatusChange(entity, null, tickingState, Visibility.HIDDEN, false, false, reason.shouldDestroy());

            this.entity.setLevelCallback(NoOpCallback.INSTANCE);
        }
    }

    private static final class NoOpCallback implements EntityInLevelCallback {

        public static final NoOpCallback INSTANCE = new NoOpCallback();

        @Override
        public void onMove() {}

        @Override
        public void onRemove(final Entity.RemovalReason reason) {}
    }
}
