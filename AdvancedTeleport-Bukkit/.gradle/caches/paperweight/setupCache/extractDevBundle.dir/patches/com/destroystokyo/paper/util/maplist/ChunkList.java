package com.destroystokyo.paper.util.maplist;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.minecraft.world.level.chunk.LevelChunk;

// list with O(1) remove & contains
/**
 * @author Spottedleaf
 */
public final class ChunkList implements Iterable<LevelChunk> {

    protected final Long2IntOpenHashMap chunkToIndex = new Long2IntOpenHashMap(2, 0.8f);
    {
        this.chunkToIndex.defaultReturnValue(Integer.MIN_VALUE);
    }

    protected static final LevelChunk[] EMPTY_LIST = new LevelChunk[0];

    protected LevelChunk[] chunks = EMPTY_LIST;
    protected int count;

    public int size() {
        return this.count;
    }

    public boolean contains(final LevelChunk chunk) {
        return this.chunkToIndex.containsKey(chunk.coordinateKey);
    }

    public boolean remove(final LevelChunk chunk) {
        final int index = this.chunkToIndex.remove(chunk.coordinateKey);
        if (index == Integer.MIN_VALUE) {
            return false;
        }

        // move the entity at the end to this index
        final int endIndex = --this.count;
        final LevelChunk end = this.chunks[endIndex];
        if (index != endIndex) {
            // not empty after this call
            this.chunkToIndex.put(end.coordinateKey, index); // update index
        }
        this.chunks[index] = end;
        this.chunks[endIndex] = null;

        return true;
    }

    public boolean add(final LevelChunk chunk) {
        final int count = this.count;
        final int currIndex = this.chunkToIndex.putIfAbsent(chunk.coordinateKey, count);

        if (currIndex != Integer.MIN_VALUE) {
            return false; // already in this list
        }

        LevelChunk[] list = this.chunks;

        if (list.length == count) {
            // resize required
            list = this.chunks = Arrays.copyOf(list, (int)Math.max(4L, count * 2L)); // overflow results in negative
        }

        list[count] = chunk;
        this.count = count + 1;

        return true;
    }

    public LevelChunk getChecked(final int index) {
        if (index < 0 || index >= this.count) {
            throw new IndexOutOfBoundsException("Index: " + index + " is out of bounds, size: " + this.count);
        }
        return this.chunks[index];
    }

    public LevelChunk getUnchecked(final int index) {
        return this.chunks[index];
    }

    public LevelChunk[] getRawData() {
        return this.chunks;
    }

    public void clear() {
        this.chunkToIndex.clear();
        Arrays.fill(this.chunks, 0, this.count, null);
        this.count = 0;
    }

    @Override
    public Iterator<LevelChunk> iterator() {
        return new Iterator<LevelChunk>() {

            LevelChunk lastRet;
            int current;

            @Override
            public boolean hasNext() {
                return this.current < ChunkList.this.count;
            }

            @Override
            public LevelChunk next() {
                if (this.current >= ChunkList.this.count) {
                    throw new NoSuchElementException();
                }
                return this.lastRet = ChunkList.this.chunks[this.current++];
            }

            @Override
            public void remove() {
                final LevelChunk lastRet = this.lastRet;

                if (lastRet == null) {
                    throw new IllegalStateException();
                }
                this.lastRet = null;

                ChunkList.this.remove(lastRet);
                --this.current;
            }
        };
    }
}
