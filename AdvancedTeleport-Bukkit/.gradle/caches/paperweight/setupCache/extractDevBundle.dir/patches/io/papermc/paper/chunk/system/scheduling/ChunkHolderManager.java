package io.papermc.paper.chunk.system.scheduling;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import ca.spottedleaf.concurrentutil.map.SWMRLong2ObjectHashTable;
import co.aikar.timings.Timing;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import io.papermc.paper.chunk.system.io.RegionFileIOThread;
import io.papermc.paper.chunk.system.poi.PoiChunk;
import io.papermc.paper.util.CoordinateUtils;
import io.papermc.paper.util.TickThread;
import io.papermc.paper.util.misc.Delayed8WayDistancePropagator2D;
import io.papermc.paper.world.ChunkEntitySlices;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import io.papermc.paper.chunk.system.ChunkSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public final class ChunkHolderManager {

    private static final Logger LOGGER = LogUtils.getClassLogger();

    public static final int FULL_LOADED_TICKET_LEVEL    = 33;
    public static final int BLOCK_TICKING_TICKET_LEVEL  = 32;
    public static final int ENTITY_TICKING_TICKET_LEVEL = 31;
    public static final int MAX_TICKET_LEVEL = ChunkMap.MAX_CHUNK_DISTANCE; // inclusive

    private static final long NO_TIMEOUT_MARKER = -1L;

    final ReentrantLock ticketLock = new ReentrantLock();

    private final SWMRLong2ObjectHashTable<NewChunkHolder> chunkHolders = new SWMRLong2ObjectHashTable<>(16384, 0.25f);
    private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>(8192, 0.25f);
    // what a disaster of a name
    // this is a map of removal tick to a map of chunks and the number of tickets a chunk has that are to expire that tick
    private final Long2ObjectOpenHashMap<Long2IntOpenHashMap> removeTickToChunkExpireTicketCount = new Long2ObjectOpenHashMap<>();
    private final ServerLevel world;
    private final ChunkTaskScheduler taskScheduler;
    private long currentTick;

    private final ArrayDeque<NewChunkHolder> pendingFullLoadUpdate = new ArrayDeque<>();
    private final ObjectRBTreeSet<NewChunkHolder> autoSaveQueue = new ObjectRBTreeSet<>((final NewChunkHolder c1, final NewChunkHolder c2) -> {
        if (c1 == c2) {
            return 0;
        }

        final int saveTickCompare = Long.compare(c1.lastAutoSave, c2.lastAutoSave);

        if (saveTickCompare != 0) {
            return saveTickCompare;
        }

        final long coord1 = CoordinateUtils.getChunkKey(c1.chunkX, c1.chunkZ);
        final long coord2 = CoordinateUtils.getChunkKey(c2.chunkX, c2.chunkZ);

        if (coord1 == coord2) {
            throw new IllegalStateException("Duplicate chunkholder in auto save queue");
        }

        return Long.compare(coord1, coord2);
    });

    public ChunkHolderManager(final ServerLevel world, final ChunkTaskScheduler taskScheduler) {
        this.world = world;
        this.taskScheduler = taskScheduler;
    }

    private long statusUpgradeId;

    long getNextStatusUpgradeId() {
        return ++this.statusUpgradeId;
    }

    public List<ChunkHolder> getOldChunkHolders() {
        final List<NewChunkHolder> holders = this.getChunkHolders();
        final List<ChunkHolder> ret = new ArrayList<>(holders.size());
        for (final NewChunkHolder holder : holders) {
            ret.add(holder.vanillaChunkHolder);
        }
        return ret;
    }

    public List<NewChunkHolder> getChunkHolders() {
        final List<NewChunkHolder> ret = new ArrayList<>(this.chunkHolders.size());
        this.chunkHolders.forEachValue(ret::add);
        return ret;
    }

    public int size() {
        return this.chunkHolders.size();
    }

    public void close(final boolean save, final boolean halt) {
        TickThread.ensureTickThread("Closing world off-main");
        if (halt) {
            LOGGER.info("Waiting 60s for chunk system to halt for world '" + this.world.getWorld().getName() + "'");
            if (!this.taskScheduler.halt(true, TimeUnit.SECONDS.toNanos(60L))) {
                LOGGER.warn("Failed to halt world generation/loading tasks for world '" + this.world.getWorld().getName() + "'");
            } else {
                LOGGER.info("Halted chunk system for world '" + this.world.getWorld().getName() + "'");
            }
        }

        if (save) {
            this.saveAllChunks(true, true, true);
        }

        if (this.world.chunkDataControllerNew.hasTasks() || this.world.entityDataControllerNew.hasTasks() || this.world.poiDataControllerNew.hasTasks()) {
            RegionFileIOThread.flush();
        }

        // kill regionfile cache
        try {
            this.world.chunkDataControllerNew.getCache().close();
        } catch (final IOException ex) {
            LOGGER.error("Failed to close chunk regionfile cache for world '" + this.world.getWorld().getName() + "'", ex);
        }
        try {
            this.world.entityDataControllerNew.getCache().close();
        } catch (final IOException ex) {
            LOGGER.error("Failed to close entity regionfile cache for world '" + this.world.getWorld().getName() + "'", ex);
        }
        try {
            this.world.poiDataControllerNew.getCache().close();
        } catch (final IOException ex) {
            LOGGER.error("Failed to close poi regionfile cache for world '" + this.world.getWorld().getName() + "'", ex);
        }
    }

    void ensureInAutosave(final NewChunkHolder holder) {
        if (!this.autoSaveQueue.contains(holder)) {
            holder.lastAutoSave = MinecraftServer.currentTick;
            this.autoSaveQueue.add(holder);
        }
    }

    public void autoSave() {
        final List<NewChunkHolder> reschedule = new ArrayList<>();
        final long currentTick = MinecraftServer.currentTickLong;
        final long maxSaveTime = currentTick - this.world.paperConfig().chunks.autoSaveInterval.value();
        for (int autoSaved = 0; autoSaved < this.world.paperConfig().chunks.maxAutoSaveChunksPerTick && !this.autoSaveQueue.isEmpty();) {
            final NewChunkHolder holder = this.autoSaveQueue.first();

            if (holder.lastAutoSave > maxSaveTime) {
                break;
            }

            this.autoSaveQueue.remove(holder);

            holder.lastAutoSave = currentTick;
            if (holder.save(false, false) != null) {
                ++autoSaved;
            }

            if (holder.getChunkStatus().isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                reschedule.add(holder);
            }
        }

        for (final NewChunkHolder holder : reschedule) {
            if (holder.getChunkStatus().isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
                this.autoSaveQueue.add(holder);
            }
        }
    }

    public void saveAllChunks(final boolean flush, final boolean shutdown, final boolean logProgress) {
        final List<NewChunkHolder> holders = this.getChunkHolders();

        if (logProgress) {
            LOGGER.info("Saving all chunkholders for world '" + this.world.getWorld().getName() + "'");
        }

        final DecimalFormat format = new DecimalFormat("#0.00");

        int saved = 0;

        long start = System.nanoTime();
        long lastLog = start;
        boolean needsFlush = false;
        final int flushInterval = 50;

        int savedChunk = 0;
        int savedEntity = 0;
        int savedPoi = 0;

        for (int i = 0, len = holders.size(); i < len; ++i) {
            final NewChunkHolder holder = holders.get(i);
            try {
                final NewChunkHolder.SaveStat saveStat = holder.save(shutdown, false);
                if (saveStat != null) {
                    ++saved;
                    needsFlush = flush;
                    if (saveStat.savedChunk()) {
                        ++savedChunk;
                    }
                    if (saveStat.savedEntityChunk()) {
                        ++savedEntity;
                    }
                    if (saveStat.savedPoiChunk()) {
                        ++savedPoi;
                    }
                }
            } catch (final ThreadDeath thr) {
                throw thr;
            } catch (final Throwable thr) {
                LOGGER.error("Failed to save chunk (" + holder.chunkX + "," + holder.chunkZ + ") in world '" + this.world.getWorld().getName() + "'", thr);
            }
            if (needsFlush && (saved % flushInterval) == 0) {
                needsFlush = false;
                RegionFileIOThread.partialFlush(flushInterval / 2);
            }
            if (logProgress) {
                final long currTime = System.nanoTime();
                if ((currTime - lastLog) > TimeUnit.SECONDS.toNanos(10L)) {
                    lastLog = currTime;
                    LOGGER.info("Saved " + saved + " chunks (" + format.format((double)(i+1)/(double)len * 100.0) + "%) in world '" + this.world.getWorld().getName() + "'");
                }
            }
        }
        if (flush) {
            RegionFileIOThread.flush();
        }
        if (logProgress) {
            LOGGER.info("Saved " + savedChunk + " block chunks, " + savedEntity + " entity chunks, " + savedPoi + " poi chunks in world '" + this.world.getWorld().getName() + "' in " + format.format(1.0E-9 * (System.nanoTime() - start)) + "s");
        }
    }

    protected final Long2IntLinkedOpenHashMap ticketLevelUpdates = new Long2IntLinkedOpenHashMap() {
        @Override
        protected void rehash(final int newN) {
            // no downsizing allowed
            if (newN < this.n) {
                return;
            }
            super.rehash(newN);
        }
    };

    protected final Delayed8WayDistancePropagator2D ticketLevelPropagator = new Delayed8WayDistancePropagator2D(
            (final long coordinate, final byte oldLevel, final byte newLevel) -> {
                ChunkHolderManager.this.ticketLevelUpdates.putAndMoveToLast(coordinate, convertBetweenTicketLevels(newLevel));
            }
    );
    // function for converting between ticket levels and propagator levels and vice versa
    // the problem is the ticket level propagator will propagate from a set source down to zero, whereas mojang expects
    // levels to propagate from a set value up to a maximum value. so we need to convert the levels we put into the propagator
    // and the levels we get out of the propagator

    public static int convertBetweenTicketLevels(final int level) {
        return ChunkMap.MAX_CHUNK_DISTANCE - level + 1;
    }

    public boolean hasTickets() {
        this.ticketLock.lock();
        try {
            return !this.tickets.isEmpty();
        } finally {
            this.ticketLock.unlock();
        }
    }

    public String getTicketDebugString(final long coordinate) {
        this.ticketLock.lock();
        try {
            final SortedArraySet<Ticket<?>> tickets = this.tickets.get(coordinate);

            return tickets != null ? tickets.first().toString() : "no_ticket";
        } finally {
            this.ticketLock.unlock();
        }
    }

    public Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> getTicketsCopy() {
        this.ticketLock.lock();
        try {
            return this.tickets.clone();
        } finally {
            this.ticketLock.unlock();
        }
    }

    public Collection<Plugin> getPluginChunkTickets(int x, int z) {
        ImmutableList.Builder<Plugin> ret;
        this.ticketLock.lock();
        try {
            SortedArraySet<Ticket<?>> tickets = this.tickets.get(ChunkPos.asLong(x, z));

            if (tickets == null) {
                return Collections.emptyList();
            }

            ret = ImmutableList.builder();
            for (Ticket<?> ticket : tickets) {
                if (ticket.getType() == TicketType.PLUGIN_TICKET) {
                    ret.add((Plugin)ticket.key);
                }
            }
        } finally {
            this.ticketLock.unlock();
        }

        return ret.build();
    }

    protected final int getPropagatedTicketLevel(final long coordinate) {
        return convertBetweenTicketLevels(this.ticketLevelPropagator.getLevel(coordinate));
    }

    protected final void updateTicketLevel(final long coordinate, final int ticketLevel) {
        if (ticketLevel > ChunkMap.MAX_CHUNK_DISTANCE) {
            this.ticketLevelPropagator.removeSource(coordinate);
        } else {
            this.ticketLevelPropagator.setSource(coordinate, convertBetweenTicketLevels(ticketLevel));
        }
    }

    private static int getTicketLevelAt(SortedArraySet<Ticket<?>> tickets) {
        return !tickets.isEmpty() ? tickets.first().getTicketLevel() : MAX_TICKET_LEVEL + 1;
    }

    public <T> boolean addTicketAtLevel(final TicketType<T> type, final ChunkPos chunkPos, final int level,
                                        final T identifier) {
        return this.addTicketAtLevel(type, CoordinateUtils.getChunkKey(chunkPos), level, identifier);
    }

    public <T> boolean addTicketAtLevel(final TicketType<T> type, final int chunkX, final int chunkZ, final int level,
                                        final T identifier) {
        return this.addTicketAtLevel(type, CoordinateUtils.getChunkKey(chunkX, chunkZ), level, identifier);
    }

    // supposed to return true if the ticket was added and did not replace another
    // but, we always return false if the ticket cannot be added
    public <T> boolean addTicketAtLevel(final TicketType<T> type, final long chunk, final int level, final T identifier) {
        final long removeDelay = Math.max(0, type.timeout);
        if (level > MAX_TICKET_LEVEL) {
            return false;
        }

        this.ticketLock.lock();
        try {
            final long removeTick = removeDelay == 0 ? NO_TIMEOUT_MARKER : this.currentTick + removeDelay;
            final Ticket<T> ticket = new Ticket<>(type, level, identifier, removeTick);

            final SortedArraySet<Ticket<?>> ticketsAtChunk = this.tickets.computeIfAbsent(chunk, (final long keyInMap) -> {
                return SortedArraySet.create(4);
            });

            final int levelBefore = getTicketLevelAt(ticketsAtChunk);
            final Ticket<T> current = (Ticket<T>)ticketsAtChunk.replace(ticket);
            final int levelAfter = getTicketLevelAt(ticketsAtChunk);

            if (current != ticket) {
                final long oldRemovalTick = current.removalTick;
                if (removeTick != oldRemovalTick) {
                    if (oldRemovalTick != NO_TIMEOUT_MARKER) {
                        final Long2IntOpenHashMap removeCounts = this.removeTickToChunkExpireTicketCount.get(oldRemovalTick);
                        final int prevCount = removeCounts.addTo(chunk, -1);

                        if (prevCount == 1) {
                            removeCounts.remove(chunk);
                            if (removeCounts.isEmpty()) {
                                this.removeTickToChunkExpireTicketCount.remove(oldRemovalTick);
                            }
                        }
                    }
                    if (removeTick != NO_TIMEOUT_MARKER) {
                        this.removeTickToChunkExpireTicketCount.computeIfAbsent(removeTick, (final long keyInMap) -> {
                            return new Long2IntOpenHashMap();
                        }).addTo(chunk, 1);
                    }
                }
            } else {
                if (removeTick != NO_TIMEOUT_MARKER) {
                    this.removeTickToChunkExpireTicketCount.computeIfAbsent(removeTick, (final long keyInMap) -> {
                        return new Long2IntOpenHashMap();
                    }).addTo(chunk, 1);
                }
            }

            if (levelBefore != levelAfter) {
                this.updateTicketLevel(chunk, levelAfter);
            }

            return current == ticket;
        } finally {
            this.ticketLock.unlock();
        }
    }

    public <T> boolean removeTicketAtLevel(final TicketType<T> type, final ChunkPos chunkPos, final int level, final T identifier) {
        return this.removeTicketAtLevel(type, CoordinateUtils.getChunkKey(chunkPos), level, identifier);
    }

    public <T> boolean removeTicketAtLevel(final TicketType<T> type, final int chunkX, final int chunkZ, final int level, final T identifier) {
        return this.removeTicketAtLevel(type, CoordinateUtils.getChunkKey(chunkX, chunkZ), level, identifier);
    }

    public <T> boolean removeTicketAtLevel(final TicketType<T> type, final long chunk, final int level, final T identifier) {
        if (level > MAX_TICKET_LEVEL) {
            return false;
        }

        this.ticketLock.lock();
        try {
            final SortedArraySet<Ticket<?>> ticketsAtChunk = this.tickets.get(chunk);
            if (ticketsAtChunk == null) {
                return false;
            }

            final int oldLevel = getTicketLevelAt(ticketsAtChunk);
            final Ticket<T> ticket = (Ticket<T>)ticketsAtChunk.removeAndGet(new Ticket<>(type, level, identifier, -2L));

            if (ticket == null) {
                return false;
            }

            if (ticketsAtChunk.isEmpty()) {
                this.tickets.remove(chunk);
            }

            final int newLevel = getTicketLevelAt(ticketsAtChunk);

            final long removeTick = ticket.removalTick;
            if (removeTick != NO_TIMEOUT_MARKER) {
                final Long2IntOpenHashMap removeCounts = this.removeTickToChunkExpireTicketCount.get(removeTick);
                final int currCount = removeCounts.addTo(chunk, -1);

                if (currCount == 1) {
                    removeCounts.remove(chunk);
                    if (removeCounts.isEmpty()) {
                        this.removeTickToChunkExpireTicketCount.remove(removeTick);
                    }
                }
            }

            if (oldLevel != newLevel) {
                this.updateTicketLevel(chunk, newLevel);
            }

            return true;
        } finally {
            this.ticketLock.unlock();
        }
    }

    // atomic with respect to all add/remove/addandremove ticket calls for the given chunk
    public <T, V> void addAndRemoveTickets(final long chunk, final TicketType<T> addType, final int addLevel, final T addIdentifier,
                                           final TicketType<V> removeType, final int removeLevel, final V removeIdentifier) {
        this.ticketLock.lock();
        try {
            this.addTicketAtLevel(addType, chunk, addLevel, addIdentifier);
            this.removeTicketAtLevel(removeType, chunk, removeLevel, removeIdentifier);
        } finally {
            this.ticketLock.unlock();
        }
    }

    public <T> void removeAllTicketsFor(final TicketType<T> ticketType, final int ticketLevel, final T ticketIdentifier) {
        if (ticketLevel > MAX_TICKET_LEVEL) {
            return;
        }

        this.ticketLock.lock();
        try {
            for (final LongIterator iterator = new LongArrayList(this.tickets.keySet()).longIterator(); iterator.hasNext();) {
                final long chunk = iterator.nextLong();

                this.removeTicketAtLevel(ticketType, chunk, ticketLevel, ticketIdentifier);
            }
        } finally {
            this.ticketLock.unlock();
        }
    }

    public void tick() {
        TickThread.ensureTickThread("Cannot tick ticket manager off-main");

        this.ticketLock.lock();
        try {
            final long tick = ++this.currentTick;

            final Long2IntOpenHashMap toRemove = this.removeTickToChunkExpireTicketCount.remove(tick);

            if (toRemove == null) {
                return;
            }

            final Predicate<Ticket<?>> expireNow = (final Ticket<?> ticket) -> {
                return ticket.removalTick == tick;
            };

            for (final LongIterator iterator = toRemove.keySet().longIterator(); iterator.hasNext();) {
                final long chunk = iterator.nextLong();

                final SortedArraySet<Ticket<?>> tickets = this.tickets.get(chunk);
                tickets.removeIf(expireNow);
                if (tickets.isEmpty()) {
                    this.tickets.remove(chunk);
                    this.ticketLevelPropagator.removeSource(chunk);
                } else {
                    this.ticketLevelPropagator.setSource(chunk, convertBetweenTicketLevels(tickets.first().getTicketLevel()));
                }
            }
        } finally {
            this.ticketLock.unlock();
        }

        this.processTicketUpdates();
    }

    public NewChunkHolder getChunkHolder(final int chunkX, final int chunkZ) {
        return this.chunkHolders.get(CoordinateUtils.getChunkKey(chunkX, chunkZ));
    }

    public NewChunkHolder getChunkHolder(final long position) {
        return this.chunkHolders.get(position);
    }

    public void raisePriority(final int x, final int z, final PrioritisedExecutor.Priority priority) {
        final NewChunkHolder chunkHolder = this.getChunkHolder(x, z);
        if (chunkHolder != null) {
            chunkHolder.raisePriority(priority);
        }
    }

    public void setPriority(final int x, final int z, final PrioritisedExecutor.Priority priority) {
        final NewChunkHolder chunkHolder = this.getChunkHolder(x, z);
        if (chunkHolder != null) {
            chunkHolder.setPriority(priority);
        }
    }

    public void lowerPriority(final int x, final int z, final PrioritisedExecutor.Priority priority) {
        final NewChunkHolder chunkHolder = this.getChunkHolder(x, z);
        if (chunkHolder != null) {
            chunkHolder.lowerPriority(priority);
        }
    }

    private NewChunkHolder createChunkHolder(final long position) {
        final NewChunkHolder ret = new NewChunkHolder(this.world, CoordinateUtils.getChunkX(position), CoordinateUtils.getChunkZ(position), this.taskScheduler);

        ChunkSystem.onChunkHolderCreate(this.world, ret.vanillaChunkHolder);
        ret.vanillaChunkHolder.onChunkAdd();

        return ret;
    }

    // because this function creates the chunk holder without a ticket, it is the caller's responsibility to ensure
    // the chunk holder eventually unloads. this should only be used to avoid using processTicketUpdates to create chunkholders,
    // as processTicketUpdates may call plugin logic; in every other case a ticket is appropriate
    private NewChunkHolder getOrCreateChunkHolder(final int chunkX, final int chunkZ) {
        return this.getOrCreateChunkHolder(CoordinateUtils.getChunkKey(chunkX, chunkZ));
    }

    private NewChunkHolder getOrCreateChunkHolder(final long position) {
        if (!this.ticketLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Must hold ticket level update lock!");
        }
        if (!this.taskScheduler.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Must hold scheduler lock!!");
        }

        // we could just acquire these locks, but...
        // must own the locks because the caller needs to ensure that no unload can occur AFTER this function returns

        NewChunkHolder current = this.chunkHolders.get(position);
        if (current != null) {
            return current;
        }

        current = this.createChunkHolder(position);
        this.chunkHolders.put(position, current);

        return current;
    }

    private long entityLoadCounter;

    public ChunkEntitySlices getOrCreateEntityChunk(final int chunkX, final int chunkZ, final boolean transientChunk) {
        TickThread.ensureTickThread(this.world, chunkX, chunkZ, "Cannot create entity chunk off-main");
        ChunkEntitySlices ret;

        NewChunkHolder current = this.getChunkHolder(chunkX, chunkZ);
        if (current != null && (ret = current.getEntityChunk()) != null && (transientChunk || !ret.isTransient())) {
            return ret;
        }

        final AtomicBoolean isCompleted = new AtomicBoolean();
        final Thread waiter = Thread.currentThread();
        final Long entityLoadId;
        NewChunkHolder.GenericDataLoadTaskCallback loadTask = null;
        this.ticketLock.lock();
        try {
            entityLoadId = Long.valueOf(this.entityLoadCounter++);
            this.addTicketAtLevel(TicketType.ENTITY_LOAD, chunkX, chunkZ, MAX_TICKET_LEVEL, entityLoadId);
            this.taskScheduler.schedulingLock.lock();
            try {
                current = this.getOrCreateChunkHolder(chunkX, chunkZ);
                if ((ret = current.getEntityChunk()) != null && (transientChunk || !ret.isTransient())) {
                    this.removeTicketAtLevel(TicketType.ENTITY_LOAD, chunkX, chunkZ, MAX_TICKET_LEVEL, entityLoadId);
                    return ret;
                }

                if (current.isEntityChunkNBTLoaded()) {
                    isCompleted.setPlain(true);
                } else {
                    loadTask = current.getOrLoadEntityData((final GenericDataLoadTask.TaskResult<CompoundTag, Throwable> result) -> {
                        if (!transientChunk) {
                            isCompleted.set(true);
                            LockSupport.unpark(waiter);
                        }
                    });
                    final ChunkLoadTask.EntityDataLoadTask entityLoad = current.getEntityDataLoadTask();

                    if (entityLoad != null && !transientChunk) {
                        entityLoad.raisePriority(PrioritisedExecutor.Priority.BLOCKING);
                    }
                }
            } finally {
                this.taskScheduler.schedulingLock.unlock();
            }
        } finally {
            this.ticketLock.unlock();
        }

        if (loadTask != null) {
            loadTask.schedule();
        }

        if (!transientChunk) {
            // Note: no need to busy wait on the chunk queue, entity load will complete off-main
            boolean interrupted = false;
            while (!isCompleted.get()) {
                interrupted |= Thread.interrupted();
                LockSupport.park();
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        // now that the entity data is loaded, we can load it into the world

        ret = current.loadInEntityChunk(transientChunk);

        final long chunkKey = CoordinateUtils.getChunkKey(chunkX, chunkZ);
        this.addAndRemoveTickets(chunkKey,
            TicketType.UNKNOWN, MAX_TICKET_LEVEL, new ChunkPos(chunkX, chunkZ),
            TicketType.ENTITY_LOAD, MAX_TICKET_LEVEL, entityLoadId
        );

        return ret;
    }

    public PoiChunk getPoiChunkIfLoaded(final int chunkX, final int chunkZ, final boolean checkLoadInCallback) {
        final NewChunkHolder holder = this.getChunkHolder(chunkX, chunkZ);
        if (holder != null) {
            final PoiChunk ret = holder.getPoiChunk();
            return ret == null || (checkLoadInCallback && !ret.isLoaded()) ? null : ret;
        }
        return null;
    }

    private long poiLoadCounter;

    public PoiChunk loadPoiChunk(final int chunkX, final int chunkZ) {
        TickThread.ensureTickThread(this.world, chunkX, chunkZ, "Cannot create poi chunk off-main");
        PoiChunk ret;

        NewChunkHolder current = this.getChunkHolder(chunkX, chunkZ);
        if (current != null && (ret = current.getPoiChunk()) != null) {
            if (!ret.isLoaded()) {
                ret.load();
            }
            return ret;
        }

        final AtomicReference<PoiChunk> completed = new AtomicReference<>();
        final AtomicBoolean isCompleted = new AtomicBoolean();
        final Thread waiter = Thread.currentThread();
        final Long poiLoadId;
        NewChunkHolder.GenericDataLoadTaskCallback loadTask = null;
        this.ticketLock.lock();
        try {
            poiLoadId = Long.valueOf(this.poiLoadCounter++);
            this.addTicketAtLevel(TicketType.POI_LOAD, chunkX, chunkZ, MAX_TICKET_LEVEL, poiLoadId);
            this.taskScheduler.schedulingLock.lock();
            try {
                current = this.getOrCreateChunkHolder(chunkX, chunkZ);
                if (current.isPoiChunkLoaded()) {
                    this.removeTicketAtLevel(TicketType.POI_LOAD, chunkX, chunkZ, MAX_TICKET_LEVEL, poiLoadId);
                    return current.getPoiChunk();
                }

                loadTask = current.getOrLoadPoiData((final GenericDataLoadTask.TaskResult<PoiChunk, Throwable> result) -> {
                    completed.setPlain(result.left());
                    isCompleted.set(true);
                    LockSupport.unpark(waiter);
                });
                final ChunkLoadTask.PoiDataLoadTask poiLoad = current.getPoiDataLoadTask();

                if (poiLoad != null) {
                    poiLoad.raisePriority(PrioritisedExecutor.Priority.BLOCKING);
                }
            } finally {
                this.taskScheduler.schedulingLock.unlock();
            }
        } finally {
            this.ticketLock.unlock();
        }

        if (loadTask != null) {
            loadTask.schedule();
        }

        // Note: no need to busy wait on the chunk queue, poi load will complete off-main

        boolean interrupted = false;
        while (!isCompleted.get()) {
            interrupted |= Thread.interrupted();
            LockSupport.park();
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        ret = completed.getPlain();

        ret.load();

        final long chunkKey = CoordinateUtils.getChunkKey(chunkX, chunkZ);
        this.addAndRemoveTickets(chunkKey,
            TicketType.UNKNOWN, MAX_TICKET_LEVEL, new ChunkPos(chunkX, chunkZ),
            TicketType.POI_LOAD, MAX_TICKET_LEVEL, poiLoadId
        );

        return ret;
    }

    void addChangedStatuses(final List<NewChunkHolder> changedFullStatus) {
        if (changedFullStatus.isEmpty()) {
            return;
        }
        if (!TickThread.isTickThread()) {
            this.taskScheduler.scheduleChunkTask(() -> {
                final ArrayDeque<NewChunkHolder> pendingFullLoadUpdate = ChunkHolderManager.this.pendingFullLoadUpdate;
                for (int i = 0, len = changedFullStatus.size(); i < len; ++i) {
                    pendingFullLoadUpdate.add(changedFullStatus.get(i));
                }

                ChunkHolderManager.this.processPendingFullUpdate();
            }, PrioritisedExecutor.Priority.HIGHEST);
        } else {
            final ArrayDeque<NewChunkHolder> pendingFullLoadUpdate = this.pendingFullLoadUpdate;
            for (int i = 0, len = changedFullStatus.size(); i < len; ++i) {
                pendingFullLoadUpdate.add(changedFullStatus.get(i));
            }
        }
    }

    final ReferenceLinkedOpenHashSet<NewChunkHolder> unloadQueue = new ReferenceLinkedOpenHashSet<>();

    private void removeChunkHolder(final NewChunkHolder holder) {
        holder.killed = true;
        holder.vanillaChunkHolder.onChunkRemove();
        this.autoSaveQueue.remove(holder);
        ChunkSystem.onChunkHolderDelete(this.world, holder.vanillaChunkHolder);
        this.chunkHolders.remove(CoordinateUtils.getChunkKey(holder.chunkX, holder.chunkZ));
    }

    // note: never call while inside the chunk system, this will absolutely break everything
    public void processUnloads() {
        TickThread.ensureTickThread("Cannot unload chunks off-main");

        if (BLOCK_TICKET_UPDATES.get() == Boolean.TRUE) {
            throw new IllegalStateException("Cannot unload chunks recursively");
        }
        if (this.ticketLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot hold ticket update lock while calling processUnloads");
        }
        if (this.taskScheduler.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot hold scheduling lock while calling processUnloads");
        }

        final List<NewChunkHolder.UnloadState> unloadQueue;
        final List<ChunkProgressionTask> scheduleList = new ArrayList<>();
        this.ticketLock.lock();
        try {
            this.taskScheduler.schedulingLock.lock();
            try {
                if (this.unloadQueue.isEmpty()) {
                    return;
                }
                // in order to ensure all chunks in the unload queue do not have a pending ticket level update,
                // process them now
                this.processTicketUpdates(false, false, scheduleList);
                unloadQueue = new ArrayList<>((int)(this.unloadQueue.size() * 0.05) + 1);

                final int unloadCount = Math.max(50, (int)(this.unloadQueue.size() * 0.05));
                for (int i = 0; i < unloadCount && !this.unloadQueue.isEmpty(); ++i) {
                    final NewChunkHolder chunkHolder = this.unloadQueue.removeFirst();
                    if (chunkHolder.isSafeToUnload() != null) {
                        LOGGER.error("Chunkholder " + chunkHolder + " is not safe to unload but is inside the unload queue?");
                        continue;
                    }
                    final NewChunkHolder.UnloadState state = chunkHolder.unloadStage1();
                    if (state == null) {
                        // can unload immediately
                        this.removeChunkHolder(chunkHolder);
                        continue;
                    }
                    unloadQueue.add(state);
                }
            } finally {
                this.taskScheduler.schedulingLock.unlock();
            }
        } finally {
            this.ticketLock.unlock();
        }
        // schedule tasks, we can't let processTicketUpdates do this because we call it holding the schedule lock
        for (int i = 0, len = scheduleList.size(); i < len; ++i) {
            scheduleList.get(i).schedule();
        }

        final List<NewChunkHolder> toRemove = new ArrayList<>(unloadQueue.size());

        final Boolean before = this.blockTicketUpdates();
        try {
            for (int i = 0, len = unloadQueue.size(); i < len; ++i) {
                final NewChunkHolder.UnloadState state = unloadQueue.get(i);
                final NewChunkHolder holder = state.holder();

                holder.unloadStage2(state);
                toRemove.add(holder);
            }
        } finally {
            this.unblockTicketUpdates(before);
        }

        this.ticketLock.lock();
        try {
            this.taskScheduler.schedulingLock.lock();
            try {
                for (int i = 0, len = toRemove.size(); i < len; ++i) {
                    final NewChunkHolder holder = toRemove.get(i);

                    if (holder.unloadStage3()) {
                        this.removeChunkHolder(holder);
                    } else {
                        // add cooldown so the next unload check is not immediately next tick
                        this.addTicketAtLevel(TicketType.UNLOAD_COOLDOWN, holder.chunkX, holder.chunkZ, MAX_TICKET_LEVEL, Unit.INSTANCE);
                    }
                }
            } finally {
                this.taskScheduler.schedulingLock.unlock();
            }
        } finally {
            this.ticketLock.unlock();
        }
    }

    private final ThreadLocal<Boolean> BLOCK_TICKET_UPDATES = ThreadLocal.withInitial(() -> {
        return Boolean.FALSE;
    });

    public Boolean blockTicketUpdates() {
        final Boolean ret = BLOCK_TICKET_UPDATES.get();
        BLOCK_TICKET_UPDATES.set(Boolean.TRUE);
        return ret;
    }

    public void unblockTicketUpdates(final Boolean before) {
        BLOCK_TICKET_UPDATES.set(before);
    }

    public boolean processTicketUpdates() {
        co.aikar.timings.MinecraftTimings.distanceManagerTick.startTiming(); try { // Paper - add timings for distance manager
        return this.processTicketUpdates(true, true, null);
        } finally { co.aikar.timings.MinecraftTimings.distanceManagerTick.stopTiming(); } // Paper - add timings for distance manager
    }

    private static final ThreadLocal<List<ChunkProgressionTask>> CURRENT_TICKET_UPDATE_SCHEDULING = new ThreadLocal<>();

    static List<ChunkProgressionTask> getCurrentTicketUpdateScheduling() {
        return CURRENT_TICKET_UPDATE_SCHEDULING.get();
    }

    private boolean processTicketUpdates(final boolean checkLocks, final boolean processFullUpdates, List<ChunkProgressionTask> scheduledTasks) {
        TickThread.ensureTickThread("Cannot process ticket levels off-main");
        if (BLOCK_TICKET_UPDATES.get() == Boolean.TRUE) {
            throw new IllegalStateException("Cannot update ticket level while unloading chunks or updating entity manager");
        }
        if (checkLocks && this.ticketLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Illegal recursive processTicketUpdates!");
        }
        if (checkLocks && this.taskScheduler.schedulingLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot update ticket levels from a scheduler context!");
        }

        List<NewChunkHolder> changedFullStatus = null;

        final boolean isTickThread = TickThread.isTickThread();

        boolean ret = false;
        final boolean canProcessFullUpdates = processFullUpdates & isTickThread;
        final boolean canProcessScheduling = scheduledTasks == null;

        this.ticketLock.lock();
        try {
            final boolean levelsUpdated = this.ticketLevelPropagator.propagateUpdates();
            if (levelsUpdated) {
                // Unlike CB, ticket level updates cannot happen recursively. Thank god.
                if (!this.ticketLevelUpdates.isEmpty()) {
                    ret = true;

                    // first the necessary chunkholders must be created, so just update the ticket levels
                    for (final Iterator<Long2IntMap.Entry> iterator = this.ticketLevelUpdates.long2IntEntrySet().fastIterator(); iterator.hasNext();) {
                        final Long2IntMap.Entry entry = iterator.next();
                        final long key = entry.getLongKey();
                        final int newLevel = entry.getIntValue();

                        NewChunkHolder current = this.chunkHolders.get(key);
                        if (current == null && newLevel > MAX_TICKET_LEVEL) {
                            // not loaded and it shouldn't be loaded!
                            iterator.remove();
                            continue;
                        }

                        final int currentLevel = current == null ? MAX_TICKET_LEVEL + 1 : current.getCurrentTicketLevel();
                        if (currentLevel == newLevel) {
                            // nothing to do
                            iterator.remove();
                            continue;
                        }

                        if (current == null) {
                            // must create
                            current = this.createChunkHolder(key);
                            this.chunkHolders.put(key, current);
                            current.updateTicketLevel(newLevel);
                        } else {
                            current.updateTicketLevel(newLevel);
                        }
                    }

                    if (scheduledTasks == null) {
                        scheduledTasks = new ArrayList<>();
                    }
                    changedFullStatus = new ArrayList<>();

                    // allow the chunkholders to process ticket level updates without needing to acquire the schedule lock every time
                    final List<ChunkProgressionTask> prev = CURRENT_TICKET_UPDATE_SCHEDULING.get();
                    CURRENT_TICKET_UPDATE_SCHEDULING.set(scheduledTasks);
                    try {
                        this.taskScheduler.schedulingLock.lock();
                        try {
                            for (final Iterator<Long2IntMap.Entry> iterator = this.ticketLevelUpdates.long2IntEntrySet().fastIterator(); iterator.hasNext();) {
                                final Long2IntMap.Entry entry = iterator.next();
                                final long key = entry.getLongKey();
                                final NewChunkHolder current = this.chunkHolders.get(key);

                                if (current == null) {
                                    throw new IllegalStateException("Expected chunk holder to be created");
                                }

                                current.processTicketLevelUpdate(scheduledTasks, changedFullStatus);
                            }
                        } finally {
                            this.taskScheduler.schedulingLock.unlock();
                        }
                    } finally {
                        CURRENT_TICKET_UPDATE_SCHEDULING.set(prev);
                    }

                    this.ticketLevelUpdates.clear();
                }
            }
        } finally {
            this.ticketLock.unlock();
        }

        if (changedFullStatus != null) {
            this.addChangedStatuses(changedFullStatus);
        }

        if (canProcessScheduling && scheduledTasks != null) {
            for (int i = 0, len = scheduledTasks.size(); i < len; ++i) {
                scheduledTasks.get(i).schedule();
            }
        }

        if (canProcessFullUpdates) {
            ret |= this.processPendingFullUpdate();
        }

        return ret;
    }

    // only call on tick thread
    protected final boolean processPendingFullUpdate() {
        final ArrayDeque<NewChunkHolder> pendingFullLoadUpdate = this.pendingFullLoadUpdate;

        boolean ret = false;

        List<NewChunkHolder> changedFullStatus = new ArrayList<>();

        NewChunkHolder holder;
        while ((holder = pendingFullLoadUpdate.poll()) != null) {
            ret |= holder.handleFullStatusChange(changedFullStatus);

            if (!changedFullStatus.isEmpty()) {
                for (int i = 0, len = changedFullStatus.size(); i < len; ++i) {
                    pendingFullLoadUpdate.add(changedFullStatus.get(i));
                }
                changedFullStatus.clear();
            }
        }

        return ret;
    }

    public JsonObject getDebugJsonForWatchdog() {
        // try and detect any potential deadlock that would require us to read unlocked
        try {
            if (this.ticketLock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    if (this.taskScheduler.schedulingLock.tryLock(10, TimeUnit.SECONDS)) {
                        try {
                            return this.getDebugJsonNoLock();
                        } finally {
                            this.taskScheduler.schedulingLock.unlock();
                        }
                    }
                } finally {
                    this.ticketLock.unlock();
                }
            }
        } catch (final InterruptedException ignore) {}

        LOGGER.error("Failed to acquire ticket and scheduling lock before timeout for world " + this.world.getWorld().getName());

        // because we read without locks, it may throw exceptions for fastutil maps
        // so just try until it works...
        Throwable lastException = null;
        for (int count = 0;count < 1000;++count) {
            try {
                return this.getDebugJsonNoLock();
            } catch (final ThreadDeath death) {
                throw death;
            } catch (final Throwable thr) {
                lastException = thr;
                Thread.yield();
                LockSupport.parkNanos(10_000L);
            }
        }

        // failed, return
        LOGGER.error("Failed to retrieve debug json for watchdog thread without locking", lastException);
        return null;
    }

    private JsonObject getDebugJsonNoLock() {
        final JsonObject ret = new JsonObject();
        ret.addProperty("current_tick", Long.valueOf(this.currentTick));

        final JsonArray unloadQueue = new JsonArray();
        ret.add("unload_queue", unloadQueue);
        for (final NewChunkHolder holder : this.unloadQueue) {
            final JsonObject coordinate = new JsonObject();
            unloadQueue.add(coordinate);

            coordinate.addProperty("chunkX", Integer.valueOf(holder.chunkX));
            coordinate.addProperty("chunkZ", Integer.valueOf(holder.chunkZ));
        }

        final JsonArray holders = new JsonArray();
        ret.add("chunkholders", holders);

        for (final NewChunkHolder holder : this.getChunkHolders()) {
            holders.add(holder.getDebugJson());
        }

        final JsonArray removeTickToChunkExpireTicketCount = new JsonArray();
        ret.add("remove_tick_to_chunk_expire_ticket_count", removeTickToChunkExpireTicketCount);

        for (final Long2ObjectMap.Entry<Long2IntOpenHashMap> tickEntry : this.removeTickToChunkExpireTicketCount.long2ObjectEntrySet()) {
            final long tick = tickEntry.getLongKey();
            final Long2IntOpenHashMap coordinateToCount = tickEntry.getValue();

            final JsonObject tickJson = new JsonObject();
            removeTickToChunkExpireTicketCount.add(tickJson);

            tickJson.addProperty("tick", Long.valueOf(tick));

            final JsonArray tickEntries = new JsonArray();
            tickJson.add("entries", tickEntries);

            for (final Long2IntMap.Entry entry : coordinateToCount.long2IntEntrySet()) {
                final long coordinate = entry.getLongKey();
                final int count = entry.getIntValue();

                final JsonObject entryJson = new JsonObject();
                tickEntries.add(entryJson);

                entryJson.addProperty("chunkX", Long.valueOf(CoordinateUtils.getChunkX(coordinate)));
                entryJson.addProperty("chunkZ", Long.valueOf(CoordinateUtils.getChunkZ(coordinate)));
                entryJson.addProperty("count", Integer.valueOf(count));
            }
        }

        final JsonArray allTicketsJson = new JsonArray();
        ret.add("tickets", allTicketsJson);

        for (final Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> coordinateTickets : this.tickets.long2ObjectEntrySet()) {
            final long coordinate = coordinateTickets.getLongKey();
            final SortedArraySet<Ticket<?>> tickets = coordinateTickets.getValue();

            final JsonObject coordinateJson = new JsonObject();
            allTicketsJson.add(coordinateJson);

            coordinateJson.addProperty("chunkX", Long.valueOf(CoordinateUtils.getChunkX(coordinate)));
            coordinateJson.addProperty("chunkZ", Long.valueOf(CoordinateUtils.getChunkZ(coordinate)));

            final JsonArray ticketsSerialized = new JsonArray();
            coordinateJson.add("tickets", ticketsSerialized);

            for (final Ticket<?> ticket : tickets) {
                final JsonObject ticketSerialized = new JsonObject();
                ticketsSerialized.add(ticketSerialized);

                ticketSerialized.addProperty("type", ticket.getType().toString());
                ticketSerialized.addProperty("level", Integer.valueOf(ticket.getTicketLevel()));
                ticketSerialized.addProperty("identifier", Objects.toString(ticket.key));
                ticketSerialized.addProperty("remove_tick", Long.valueOf(ticket.removalTick));
            }
        }

        return ret;
    }

    public JsonObject getDebugJson() {
        final List<ChunkProgressionTask> scheduleList = new ArrayList<>();
        try {
            final JsonObject ret;
            this.ticketLock.lock();
            try {
                this.taskScheduler.schedulingLock.lock();
                try {
                    this.processTicketUpdates(false, false, scheduleList);
                    ret = this.getDebugJsonNoLock();
                } finally {
                    this.taskScheduler.schedulingLock.unlock();
                }
            } finally {
                this.ticketLock.unlock();
            }
            return ret;
        } finally {
            // schedule tasks, we can't let processTicketUpdates do this because we call it holding the schedule lock
            for (int i = 0, len = scheduleList.size(); i < len; ++i) {
                scheduleList.get(i).schedule();
            }
        }
    }
}
