package io.papermc.paper.voxel;

import io.papermc.paper.util.CollisionUtil;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.ArrayList;
import java.util.List;

public final class AABBVoxelShape extends VoxelShape {

    public final AABB aabb;

    public AABBVoxelShape(AABB aabb) {
        super(Shapes.getFullUnoptimisedCube().shape);
        this.aabb = aabb;
    }

    @Override
    public boolean isEmpty() {
        return CollisionUtil.isEmpty(this.aabb);
    }

    @Override
    public double min(Direction.Axis enumdirection_enumaxis) {
        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                return this.aabb.minX;
            case 1:
                return this.aabb.minY;
            case 2:
                return this.aabb.minZ;
            default:
                throw new IllegalStateException("Unknown axis requested");
        }
    }

    @Override
    public double max(Direction.Axis enumdirection_enumaxis) {
        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                return this.aabb.maxX;
            case 1:
                return this.aabb.maxY;
            case 2:
                return this.aabb.maxZ;
            default:
                throw new IllegalStateException("Unknown axis requested");
        }
    }

    @Override
    public AABB bounds() {
        return this.aabb;
    }

    // enum direction axis is from 0 -> 2, so we keep the lower bits for direction axis.
    @Override
    protected double get(Direction.Axis enumdirection_enumaxis, int i) {
        switch (enumdirection_enumaxis.ordinal() | (i << 2)) {
            case (0 | (0 << 2)):
                return this.aabb.minX;
            case (1 | (0 << 2)):
                return this.aabb.minY;
            case (2 | (0 << 2)):
                return this.aabb.minZ;
            case (0 | (1 << 2)):
                return this.aabb.maxX;
            case (1 | (1 << 2)):
                return this.aabb.maxY;
            case (2 | (1 << 2)):
                return this.aabb.maxZ;
            default:
                throw new IllegalStateException("Unknown axis requested");
        }
    }

    private DoubleList cachedListX;
    private DoubleList cachedListY;
    private DoubleList cachedListZ;

    @Override
    protected DoubleList getCoords(Direction.Axis enumdirection_enumaxis) {
        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                return this.cachedListX == null ? this.cachedListX = DoubleArrayList.wrap(new double[] { this.aabb.minX, this.aabb.maxX }) : this.cachedListX;
            case 1:
                return this.cachedListY == null ? this.cachedListY = DoubleArrayList.wrap(new double[] { this.aabb.minY, this.aabb.maxY }) : this.cachedListY;
            case 2:
                return this.cachedListZ == null ? this.cachedListZ = DoubleArrayList.wrap(new double[] { this.aabb.minZ, this.aabb.maxZ }) : this.cachedListZ;
            default:
                throw new IllegalStateException("Unknown axis requested");
        }
    }

    @Override
    public VoxelShape move(double d0, double d1, double d2) {
        return new AABBVoxelShape(this.aabb.move(d0, d1, d2));
    }

    @Override
    public VoxelShape optimize() {
        if (this.isEmpty()) {
            return Shapes.empty();
        } else if (this == Shapes.BLOCK_OPTIMISED || this.aabb.equals(Shapes.BLOCK_OPTIMISED.aabb)) {
            return Shapes.BLOCK_OPTIMISED;
        }
        return this;
    }

    @Override
    public void forAllBoxes(Shapes.DoubleLineConsumer voxelshapes_a) {
        voxelshapes_a.consume(this.aabb.minX, this.aabb.minY, this.aabb.minZ, this.aabb.maxX, this.aabb.maxY, this.aabb.maxZ);
    }

    @Override
    public List<AABB> toAabbs() { // getAABBs
        List<AABB> ret = new ArrayList<>(1);
        ret.add(this.aabb);
        return ret;
    }

    @Override
    protected int findIndex(Direction.Axis enumdirection_enumaxis, double d0) { // findPointIndexAfterOffset
        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                return d0 < this.aabb.maxX ? (d0 < this.aabb.minX ? -1 : 0) : 1;
            case 1:
                return d0 < this.aabb.maxY ? (d0 < this.aabb.minY ? -1 : 0) : 1;
            case 2:
                return d0 < this.aabb.maxZ ? (d0 < this.aabb.minZ ? -1 : 0) : 1;
            default:
                throw new IllegalStateException("Unknown axis requested");
        }
    }

    @Override
    protected VoxelShape calculateFace(Direction direction) {
        if (this.isEmpty()) {
            return Shapes.empty();
        }
        if (this == Shapes.BLOCK_OPTIMISED) {
            return this;
        }
        switch (direction) {
            case EAST: // +X
            case WEST: { // -X
                final double from = direction == Direction.EAST ? 1.0 - CollisionUtil.COLLISION_EPSILON : CollisionUtil.COLLISION_EPSILON;
                if (from > this.aabb.maxX || this.aabb.minX > from) {
                    return Shapes.empty();
                }
                return new AABBVoxelShape(new AABB(0.0, this.aabb.minY, this.aabb.minZ, 1.0, this.aabb.maxY, this.aabb.maxZ)).optimize();
            }
            case UP: // +Y
            case DOWN: { // -Y
                final double from = direction == Direction.UP ? 1.0 - CollisionUtil.COLLISION_EPSILON : CollisionUtil.COLLISION_EPSILON;
                if (from > this.aabb.maxY || this.aabb.minY > from) {
                    return Shapes.empty();
                }
                return new AABBVoxelShape(new AABB(this.aabb.minX, 0.0, this.aabb.minZ, this.aabb.maxX, 1.0, this.aabb.maxZ)).optimize();
            }
            case SOUTH: // +Z
            case NORTH: { // -Z
                final double from = direction == Direction.SOUTH ? 1.0 - CollisionUtil.COLLISION_EPSILON : CollisionUtil.COLLISION_EPSILON;
                if (from > this.aabb.maxZ || this.aabb.minZ > from) {
                    return Shapes.empty();
                }
                return new AABBVoxelShape(new AABB(this.aabb.minX, this.aabb.minY, 0.0, this.aabb.maxX, this.aabb.maxY, 1.0)).optimize();
            }
            default: {
                throw new IllegalStateException("Unknown axis requested");
            }
        }
    }

    @Override
    public double collide(Direction.Axis enumdirection_enumaxis, AABB axisalignedbb, double d0) {
        if (CollisionUtil.isEmpty(this.aabb) || CollisionUtil.isEmpty(axisalignedbb)) {
            return d0;
        }
        switch (enumdirection_enumaxis.ordinal()) {
            case 0:
                return CollisionUtil.collideX(this.aabb, axisalignedbb, d0);
            case 1:
                return CollisionUtil.collideY(this.aabb, axisalignedbb, d0);
            case 2:
                return CollisionUtil.collideZ(this.aabb, axisalignedbb, d0);
            default:
                throw new IllegalStateException("Unknown axis requested");
        }
    }

    @Override
    public boolean intersects(AABB axisalingedbb) {
        return CollisionUtil.voxelShapeIntersect(this.aabb, axisalingedbb);
    }
}
