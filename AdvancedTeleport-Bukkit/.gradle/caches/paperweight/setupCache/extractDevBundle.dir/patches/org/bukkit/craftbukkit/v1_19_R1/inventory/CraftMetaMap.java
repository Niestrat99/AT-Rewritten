package org.bukkit.craftbukkit.v1_19_R1.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftMetaItem.SerializableMeta;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

@DelegateDeserialization(SerializableMeta.class)
class CraftMetaMap extends CraftMetaItem implements MapMeta {
    static final ItemMetaKey MAP_SCALING = new ItemMetaKey("map_is_scaling", "scaling");
    static final ItemMetaKey MAP_LOC_NAME = new ItemMetaKey("LocName", "display-loc-name");
    static final ItemMetaKey MAP_COLOR = new ItemMetaKey("MapColor", "display-map-color");
    static final ItemMetaKey MAP_ID = new ItemMetaKey("map", "map-id");
    static final byte SCALING_EMPTY = (byte) 0;
    static final byte SCALING_TRUE = (byte) 1;
    static final byte SCALING_FALSE = (byte) 2;

    private Integer mapId;
    private byte scaling = CraftMetaMap.SCALING_EMPTY;
    private String locName;
    private Color color;

    CraftMetaMap(CraftMetaItem meta) {
        super(meta);

        if (!(meta instanceof CraftMetaMap)) {
            return;
        }

        CraftMetaMap map = (CraftMetaMap) meta;
        this.mapId = map.mapId;
        this.scaling = map.scaling;
        this.locName = map.locName;
        this.color = map.color;
    }

    CraftMetaMap(CompoundTag tag) {
        super(tag);

        if (tag.contains(MAP_ID.NBT, CraftMagicNumbers.NBT.TAG_ANY_NUMBER)) {
            this.mapId = tag.getInt(MAP_ID.NBT);
        }

        if (tag.contains(MAP_SCALING.NBT)) {
            this.scaling = tag.getBoolean(MAP_SCALING.NBT) ? CraftMetaMap.SCALING_TRUE : CraftMetaMap.SCALING_FALSE;
        }

        if (tag.contains(DISPLAY.NBT)) {
            CompoundTag display = tag.getCompound(DISPLAY.NBT);

            if (display.contains(MAP_LOC_NAME.NBT)) {
                this.locName = display.getString(MAP_LOC_NAME.NBT);
            }

            if (display.contains(MAP_COLOR.NBT)) {
                try {
                    this.color = Color.fromRGB(display.getInt(MAP_COLOR.NBT));
                } catch (IllegalArgumentException ex) {
                    // Invalid colour
                }
            }
        }
    }

    CraftMetaMap(Map<String, Object> map) {
        super(map);

        Integer id = SerializableMeta.getObject(Integer.class, map, MAP_ID.BUKKIT, true);
        if (id != null) {
            this.setMapId(id);
        }

        Boolean scaling = SerializableMeta.getObject(Boolean.class, map, MAP_SCALING.BUKKIT, true);
        if (scaling != null) {
            this.setScaling(scaling);
        }

        String locName = SerializableMeta.getString(map, MAP_LOC_NAME.BUKKIT, true);
        if (locName != null) {
            this.setLocationName(locName);
        }

        Color color = SerializableMeta.getObject(Color.class, map, MAP_COLOR.BUKKIT, true);
        if (color != null) {
            this.setColor(color);
        }
    }

    @Override
    void applyToItem(CompoundTag tag) {
        super.applyToItem(tag);

        if (this.hasMapId()) {
            tag.putInt(MAP_ID.NBT, this.getMapId());
        }

        if (this.hasScaling()) {
            tag.putBoolean(MAP_SCALING.NBT, this.isScaling());
        }

        if (this.hasLocationName()) {
            setDisplayTag(tag, MAP_LOC_NAME.NBT, StringTag.valueOf(this.getLocationName()));
        }

        if (this.hasColor()) {
            setDisplayTag(tag, MAP_COLOR.NBT, IntTag.valueOf(this.color.asRGB()));
        }
    }

    @Override
    boolean applicableTo(Material type) {
        return type == Material.FILLED_MAP;
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && this.isMapEmpty();
    }

    boolean isMapEmpty() {
        return !(this.hasMapId() || this.hasScaling() | this.hasLocationName() || this.hasColor());
    }

    @Override
    public boolean hasMapId() {
        return this.mapId != null;
    }

    @Override
    public int getMapId() {
        Preconditions.checkState(this.hasMapView(), "Item does not have map associated - check hasMapView() first!"); // Paper - more friendly message
        return this.mapId;
    }

    @Override
    public void setMapId(int id) {
        this.mapId = id;
    }

    @Override
    public boolean hasMapView() {
        return this.mapId != null;
    }

    @Override
    public MapView getMapView() {
        Preconditions.checkState(this.hasMapView(), "Item does not have map associated - check hasMapView() first!");
        return Bukkit.getMap(mapId);
    }

    @Override
    public void setMapView(MapView map) {
        this.mapId = (map != null) ? map.getId() : null;
    }

    boolean hasScaling() {
        return this.scaling != CraftMetaMap.SCALING_EMPTY;
    }

    @Override
    public boolean isScaling() {
        return this.scaling == CraftMetaMap.SCALING_TRUE;
    }

    @Override
    public void setScaling(boolean scaling) {
        this.scaling = scaling ? CraftMetaMap.SCALING_TRUE : CraftMetaMap.SCALING_FALSE;
    }

    @Override
    public boolean hasLocationName() {
        return this.locName != null;
    }

    @Override
    public String getLocationName() {
        return this.locName;
    }

    @Override
    public void setLocationName(String name) {
        this.locName = name;
    }

    @Override
    public boolean hasColor() {
        return this.color != null;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaMap) {
            CraftMetaMap that = (CraftMetaMap) meta;

            return (this.scaling == that.scaling)
                    && (this.hasMapId() ? that.hasMapId() && this.mapId.equals(that.mapId) : !that.hasMapId())
                    && (this.hasLocationName() ? that.hasLocationName() && this.locName.equals(that.locName) : !that.hasLocationName())
                    && (this.hasColor() ? that.hasColor() && this.color.equals(that.color) : !that.hasColor());
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaMap || this.isMapEmpty());
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();

        if (this.hasMapId()) {
            hash = 61 * hash + this.mapId.hashCode();
        }
        if (this.hasScaling()) {
            hash ^= 0x22222222 << (this.isScaling() ? 1 : -1);
        }
        if (this.hasLocationName()) {
            hash = 61 * hash + this.locName.hashCode();
        }
        if (this.hasColor()) {
            hash = 61 * hash + this.color.hashCode();
        }

        return original != hash ? CraftMetaMap.class.hashCode() ^ hash : hash;
    }


    @Override
    public CraftMetaMap clone() {
        return (CraftMetaMap) super.clone();
    }

    @Override
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        super.serialize(builder);

        if (this.hasMapId()) {
            builder.put(MAP_ID.BUKKIT, this.getMapId());
        }

        if (this.hasScaling()) {
            builder.put(MAP_SCALING.BUKKIT, this.isScaling());
        }

        if (this.hasLocationName()) {
            builder.put(MAP_LOC_NAME.BUKKIT, this.getLocationName());
        }

        if (this.hasColor()) {
            builder.put(MAP_COLOR.BUKKIT, this.getColor());
        }

        return builder;
    }
}
