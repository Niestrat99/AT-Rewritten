package io.papermc.paper.chunk;

import io.papermc.paper.util.maplist.IteratorSafeOrderedReferenceSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import io.papermc.paper.util.MCUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public final class SingleThreadChunkRegionManager {

    protected final int regionSectionMergeRadius;
    protected final int regionSectionChunkSize;
    public final int regionChunkShift; // log2(REGION_CHUNK_SIZE)

    public final ServerLevel world;
    public final String name;

    protected final Long2ObjectOpenHashMap<RegionSection> regionsBySection = new Long2ObjectOpenHashMap<>();
    protected final ReferenceLinkedOpenHashSet<Region> needsRecalculation = new ReferenceLinkedOpenHashSet<>();
    protected final int minSectionRecalcCount;
    protected final double maxDeadRegionPercent;
    protected final Supplier<RegionData> regionDataSupplier;
    protected final Supplier<RegionSectionData> regionSectionDataSupplier;

    public SingleThreadChunkRegionManager(final ServerLevel world, final int minSectionRecalcCount,
                                          final double maxDeadRegionPercent, final int sectionMergeRadius,
                                          final int regionSectionChunkShift,
                                          final String name, final Supplier<RegionData> regionDataSupplier,
                                          final Supplier<RegionSectionData> regionSectionDataSupplier) {
        this.regionSectionMergeRadius = sectionMergeRadius;
        this.regionSectionChunkSize = 1 << regionSectionChunkShift;
        this.regionChunkShift = regionSectionChunkShift;
        this.world = world;
        this.name = name;
        this.minSectionRecalcCount = Math.max(2, minSectionRecalcCount);
        this.maxDeadRegionPercent = maxDeadRegionPercent;
        this.regionDataSupplier = regionDataSupplier;
        this.regionSectionDataSupplier = regionSectionDataSupplier;
    }

    // tested via https://gist.github.com/Spottedleaf/aa7ade3451c37b4cac061fc77074db2f

    /*
    protected void check() {
        ReferenceOpenHashSet<Region<T>> checked = new ReferenceOpenHashSet<>();

        for (RegionSection<T> section : this.regionsBySection.values()) {
            if (!checked.add(section.region)) {
                section.region.check();
            }
        }
        for (Region<T> region : this.needsRecalculation) {
            region.check();
        }
    }
    */

    protected void addToRecalcQueue(final Region region) {
        this.needsRecalculation.add(region);
    }

    protected void removeFromRecalcQueue(final Region region) {
        this.needsRecalculation.remove(region);
    }

    public RegionSection getRegionSection(final int chunkX, final int chunkZ) {
        return this.regionsBySection.get(MCUtil.getCoordinateKey(chunkX >> this.regionChunkShift, chunkZ >> this.regionChunkShift));
    }

    public Region getRegion(final int chunkX, final int chunkZ) {
        final RegionSection section = this.regionsBySection.get(MCUtil.getCoordinateKey(chunkX >> regionChunkShift, chunkZ >> regionChunkShift));
        return section != null ? section.region : null;
    }

    private final List<Region> toMerge = new ArrayList<>();

    protected RegionSection getOrCreateAndMergeSection(final int sectionX, final int sectionZ, final RegionSection force) {
        final long sectionKey = MCUtil.getCoordinateKey(sectionX, sectionZ);

        if (force == null) {
            RegionSection region = this.regionsBySection.get(sectionKey);
            if (region != null) {
                return region;
            }
        }

        int mergeCandidateSectionSize = -1;
        Region mergeIntoCandidate = null;

        // find optimal candidate to merge into

        final int minX = sectionX - this.regionSectionMergeRadius;
        final int maxX = sectionX + this.regionSectionMergeRadius;
        final int minZ = sectionZ - this.regionSectionMergeRadius;
        final int maxZ = sectionZ + this.regionSectionMergeRadius;
        for (int currX = minX; currX <= maxX; ++currX) {
            for (int currZ = minZ; currZ <= maxZ; ++currZ) {
                final RegionSection section = this.regionsBySection.get(MCUtil.getCoordinateKey(currX, currZ));
                if (section == null) {
                    continue;
                }
                final Region region = section.region;
                if (region.dead) {
                    throw new IllegalStateException("Dead region should not be in live region manager state: " + region);
                }
                final int sections = region.sections.size();

                if (sections > mergeCandidateSectionSize) {
                    mergeCandidateSectionSize = sections;
                    mergeIntoCandidate = region;
                }
                this.toMerge.add(region);
            }
        }

        // merge
        if (mergeIntoCandidate != null) {
            for (int i = 0; i < this.toMerge.size(); ++i) {
                final Region region = this.toMerge.get(i);
                if (region.dead || mergeIntoCandidate == region) {
                    continue;
                }
                region.mergeInto(mergeIntoCandidate);
            }
            this.toMerge.clear();
        } else {
            mergeIntoCandidate = new Region(this);
        }

        final RegionSection section;
        if (force == null) {
            this.regionsBySection.put(sectionKey, section = new RegionSection(sectionKey, this));
        } else {
            final RegionSection existing = this.regionsBySection.putIfAbsent(sectionKey, force);
            if (existing != null) {
                throw new IllegalStateException("Attempting to override section '" + existing.toStringWithRegion() +
                        ", with " + force.toStringWithRegion());
            }

            section = force;
        }

        mergeIntoCandidate.addRegionSection(section);
        //mergeIntoCandidate.check();
        //this.check();

        return section;
    }

    public void addChunk(final int chunkX, final int chunkZ) {
        this.getOrCreateAndMergeSection(chunkX >> this.regionChunkShift, chunkZ >> this.regionChunkShift, null).addChunk(chunkX, chunkZ);
    }

    public void removeChunk(final int chunkX, final int chunkZ) {
        final RegionSection section = this.regionsBySection.get(
                MCUtil.getCoordinateKey(chunkX >> this.regionChunkShift, chunkZ >> this.regionChunkShift)
        );
        if (section != null) {
            section.removeChunk(chunkX, chunkZ);
        } else {
            throw new IllegalStateException("Cannot remove chunk at (" + chunkX + "," + chunkZ + ") from region state, section does not exist");
        }
    }

    public void recalculateRegions() {
        for (int i = 0, len = this.needsRecalculation.size(); i < len; ++i) {
            final Region region = this.needsRecalculation.removeFirst();

            this.recalculateRegion(region);
            //this.check();
        }
    }

    protected void recalculateRegion(final Region region) {
        region.markedForRecalc = false;
        //region.check();
        // clear unused regions
        for (final Iterator<RegionSection> iterator = region.deadSections.iterator(); iterator.hasNext();) {
            final RegionSection deadSection = iterator.next();

            if (deadSection.hasChunks()) {
                throw new IllegalStateException("Dead section '" + deadSection.toStringWithRegion() + "' is marked dead but has chunks!");
            }
            if (!region.removeRegionSection(deadSection)) {
                throw new IllegalStateException("Region " + region + " has inconsistent state, it should contain section " + deadSection);
            }
            if (!this.regionsBySection.remove(deadSection.regionCoordinate, deadSection)) {
                throw new IllegalStateException("Cannot remove dead section '" +
                        deadSection.toStringWithRegion() + "' from section state! State at section coordinate: " +
                        this.regionsBySection.get(deadSection.regionCoordinate));
            }
        }
        region.deadSections.clear();

        // implicitly cover cases where size == 0
        if (region.sections.size() < this.minSectionRecalcCount) {
            //region.check();
            return;
        }

        // run a test to see if we actually need to recalculate
        // TODO

        // destroy and rebuild the region
        region.dead = true;

        // destroy region state
        for (final Iterator<RegionSection> iterator = region.sections.unsafeIterator(IteratorSafeOrderedReferenceSet.ITERATOR_FLAG_SEE_ADDITIONS); iterator.hasNext();) {
            final RegionSection aliveSection = iterator.next();
            if (!aliveSection.hasChunks()) {
                throw new IllegalStateException("Alive section '" + aliveSection.toStringWithRegion() + "' has no chunks!");
            }
            if (!this.regionsBySection.remove(aliveSection.regionCoordinate, aliveSection)) {
                throw new IllegalStateException("Cannot remove alive section '" +
                        aliveSection.toStringWithRegion() + "' from section state! State at section coordinate: " +
                        this.regionsBySection.get(aliveSection.regionCoordinate));
            }
        }

        // rebuild regions
        for (final Iterator<RegionSection> iterator = region.sections.unsafeIterator(IteratorSafeOrderedReferenceSet.ITERATOR_FLAG_SEE_ADDITIONS); iterator.hasNext();) {
            final RegionSection aliveSection = iterator.next();
            this.getOrCreateAndMergeSection(aliveSection.getSectionX(), aliveSection.getSectionZ(), aliveSection);
        }
    }

    public static final class Region {
        protected final IteratorSafeOrderedReferenceSet<RegionSection> sections = new IteratorSafeOrderedReferenceSet<>();
        protected final ReferenceOpenHashSet<RegionSection> deadSections = new ReferenceOpenHashSet<>(16, 0.7f);
        protected boolean dead;
        protected boolean markedForRecalc;

        public final SingleThreadChunkRegionManager regionManager;
        public final RegionData regionData;

        protected Region(final SingleThreadChunkRegionManager regionManager) {
            this.regionManager = regionManager;
            this.regionData = regionManager.regionDataSupplier.get();
        }

        public IteratorSafeOrderedReferenceSet.Iterator<RegionSection> getSections() {
            return this.sections.iterator(IteratorSafeOrderedReferenceSet.ITERATOR_FLAG_SEE_ADDITIONS);
        }

        protected final double getDeadSectionPercent() {
            return (double)this.deadSections.size() / (double)this.sections.size();
        }

        /*
        protected void check() {
            if (this.dead) {
                throw new IllegalStateException("Dead region!");
            }
            for (final Iterator<RegionSection<T>> iterator = this.sections.unsafeIterator(IteratorSafeOrderedReferenceSet.ITERATOR_FLAG_SEE_ADDITIONS); iterator.hasNext();) {
                final RegionSection<T> section = iterator.next();
                if (section.region != this) {
                    throw new IllegalStateException("Region section must point to us!");
                }
                if (this.regionManager.regionsBySection.get(section.regionCoordinate) != section) {
                    throw new IllegalStateException("Region section must match the regionmanager state!");
                }
            }
        }
        */

        // note: it is not true that the region at this point is not in any region. use the region field on the section
        // to see if it is currently in another region.
        protected final boolean addRegionSection(final RegionSection section) {
            if (!this.sections.add(section)) {
                return false;
            }

            section.sectionData.addToRegion(section, section.region, this);

            section.region = this;
            return true;
        }

        protected final boolean removeRegionSection(final RegionSection section) {
            if (!this.sections.remove(section)) {
                return false;
            }

            section.sectionData.removeFromRegion(section, this);

            return true;
        }

        protected void mergeInto(final Region mergeTarget) {
            if (this == mergeTarget) {
                throw new IllegalStateException("Cannot merge a region onto itself");
            }
            if (this.dead) {
                throw new IllegalStateException("Source region is dead! Source " + this + ", target " + mergeTarget);
            } else if (mergeTarget.dead) {
                throw new IllegalStateException("Target region is dead! Source " + this + ", target " + mergeTarget);
            }
            this.dead = true;
            if (this.markedForRecalc) {
                this.regionManager.removeFromRecalcQueue(this);
            }

            for (final Iterator<RegionSection> iterator = this.sections.unsafeIterator(IteratorSafeOrderedReferenceSet.ITERATOR_FLAG_SEE_ADDITIONS); iterator.hasNext();) {
                final RegionSection section = iterator.next();

                if (!mergeTarget.addRegionSection(section)) {
                    throw new IllegalStateException("Target cannot contain source's sections! Source " + this + ", target " + mergeTarget);
                }
            }

            for (final RegionSection deadSection : this.deadSections) {
                if (!this.sections.contains(deadSection)) {
                    throw new IllegalStateException("Source region does not even contain its own dead sections! Missing " + deadSection + " from region " + this);
                }
                mergeTarget.deadSections.add(deadSection);
            }
            //mergeTarget.check();
        }

        protected void markSectionAlive(final RegionSection section) {
            this.deadSections.remove(section);
            if (this.markedForRecalc && (this.sections.size() < this.regionManager.minSectionRecalcCount || this.getDeadSectionPercent() < this.regionManager.maxDeadRegionPercent)) {
                this.regionManager.removeFromRecalcQueue(this);
                this.markedForRecalc = false;
            }
        }

        protected void markSectionDead(final RegionSection section) {
            this.deadSections.add(section);
            if (!this.markedForRecalc && (this.sections.size() >= this.regionManager.minSectionRecalcCount || this.sections.size() == this.deadSections.size()) && this.getDeadSectionPercent() >= this.regionManager.maxDeadRegionPercent) {
                this.regionManager.addToRecalcQueue(this);
                this.markedForRecalc = true;
            }
        }

        @Override
        public String toString() {
            final StringBuilder ret = new StringBuilder(128);

            ret.append("Region{");
            ret.append("dead=").append(this.dead).append(',');
            ret.append("markedForRecalc=").append(this.markedForRecalc).append(',');

            ret.append("sectionCount=").append(this.sections.size()).append(',');
            ret.append("sections=[");
            for (final Iterator<RegionSection> iterator = this.sections.unsafeIterator(IteratorSafeOrderedReferenceSet.ITERATOR_FLAG_SEE_ADDITIONS); iterator.hasNext();) {
                final RegionSection section = iterator.next();
                ret.append(section);
                if (iterator.hasNext()) {
                    ret.append(',');
                }
            }
            ret.append(']');

            ret.append('}');
            return ret.toString();
        }
    }

    public static final class RegionSection {
        protected final long regionCoordinate;
        protected final long[] chunksBitset;
        protected int chunkCount;
        protected Region region;

        public final SingleThreadChunkRegionManager regionManager;
        public final RegionSectionData sectionData;

        protected RegionSection(final long regionCoordinate, final SingleThreadChunkRegionManager regionManager) {
            this.regionCoordinate = regionCoordinate;
            this.regionManager = regionManager;
            this.chunksBitset = new long[Math.max(1, regionManager.regionSectionChunkSize * regionManager.regionSectionChunkSize / Long.SIZE)];
            this.sectionData = regionManager.regionSectionDataSupplier.get();
        }

        public int getSectionX() {
            return MCUtil.getCoordinateX(this.regionCoordinate);
        }

        public int getSectionZ() {
            return MCUtil.getCoordinateZ(this.regionCoordinate);
        }

        public Region getRegion() {
            return this.region;
        }

        private int getChunkIndex(final int chunkX, final int chunkZ) {
            return (chunkX & (this.regionManager.regionSectionChunkSize - 1)) | ((chunkZ & (this.regionManager.regionSectionChunkSize - 1)) << this.regionManager.regionChunkShift);
        }

        protected boolean hasChunks() {
            return this.chunkCount != 0;
        }

        protected void addChunk(final int chunkX, final int chunkZ) {
            final int index = this.getChunkIndex(chunkX, chunkZ);
            final long bitset = this.chunksBitset[index >>> 6]; // index / Long.SIZE
            final long after = this.chunksBitset[index >>> 6] = bitset | (1L << (index & (Long.SIZE - 1)));
            if (after == bitset) {
                throw new IllegalStateException("Cannot add a chunk to a section which already has the chunk! RegionSection: " + this + ", global chunk: " + new ChunkPos(chunkX, chunkZ).toString());
            }
            if (++this.chunkCount != 1) {
                return;
            }
            this.region.markSectionAlive(this);
        }

        protected void removeChunk(final int chunkX, final int chunkZ) {
            final int index = this.getChunkIndex(chunkX, chunkZ);
            final long before = this.chunksBitset[index >>> 6]; // index / Long.SIZE
            final long bitset = this.chunksBitset[index >>> 6] = before & ~(1L << (index & (Long.SIZE - 1)));
            if (before == bitset) {
                throw new IllegalStateException("Cannot remove a chunk from a section which does not have that chunk! RegionSection: " + this + ", global chunk: " + new ChunkPos(chunkX, chunkZ).toString());
            }
            if (--this.chunkCount != 0) {
                return;
            }
            this.region.markSectionDead(this);
        }

        @Override
        public String toString() {
            return "RegionSection{" +
                    "regionCoordinate=" + new ChunkPos(this.regionCoordinate).toString() + "," +
                    "chunkCount=" + this.chunkCount + "," +
                    "chunksBitset=" + toString(this.chunksBitset) + "," +
                    "hash=" + this.hashCode() +
                    "}";
        }

        public String toStringWithRegion() {
            return "RegionSection{" +
                    "regionCoordinate=" + new ChunkPos(this.regionCoordinate).toString() + "," +
                    "chunkCount=" + this.chunkCount + "," +
                    "chunksBitset=" + toString(this.chunksBitset) + "," +
                    "hash=" + this.hashCode() + "," +
                    "region=" + this.region +
                    "}";
        }

        private static String toString(final long[] array) {
            final StringBuilder ret = new StringBuilder();
            for (final long value : array) {
                // zero pad the hex string
                final char[] zeros = new char[Long.SIZE / 4];
                Arrays.fill(zeros, '0');
                final String string = Long.toHexString(value);
                System.arraycopy(string.toCharArray(), 0, zeros, zeros.length - string.length(), string.length());

                ret.append(zeros);
            }

            return ret.toString();
        }
    }

    public static interface RegionData {

    }

    public static interface RegionSectionData {

        public void removeFromRegion(final RegionSection section, final Region from);

        // removal from the old region is handled via removeFromRegion
        public void addToRegion(final RegionSection section, final Region oldRegion, final Region newRegion);

    }
}
