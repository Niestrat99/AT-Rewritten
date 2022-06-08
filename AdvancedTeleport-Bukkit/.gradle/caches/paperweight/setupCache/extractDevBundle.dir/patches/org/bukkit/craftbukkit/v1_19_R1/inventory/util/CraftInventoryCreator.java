package org.bukkit.craftbukkit.v1_19_R1.inventory.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class CraftInventoryCreator {

    public static final CraftInventoryCreator INSTANCE = new CraftInventoryCreator();
    //
    private final CraftCustomInventoryConverter DEFAULT_CONVERTER = new CraftCustomInventoryConverter();
    private final Map<InventoryType, InventoryConverter> converterMap = new HashMap<>();

    private CraftInventoryCreator() {
        this.converterMap.put(InventoryType.CHEST, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.DISPENSER, new CraftTileInventoryConverter.Dispenser());
        this.converterMap.put(InventoryType.DROPPER, new CraftTileInventoryConverter.Dropper());
        this.converterMap.put(InventoryType.FURNACE, new CraftTileInventoryConverter.Furnace());
        this.converterMap.put(InventoryType.WORKBENCH, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.ENCHANTING, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.BREWING, new CraftTileInventoryConverter.BrewingStand());
        this.converterMap.put(InventoryType.PLAYER, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.MERCHANT, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.ENDER_CHEST, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.ANVIL, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.SMITHING, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.BEACON, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.HOPPER, new CraftTileInventoryConverter.Hopper());
        this.converterMap.put(InventoryType.SHULKER_BOX, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.BARREL, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.BLAST_FURNACE, new CraftTileInventoryConverter.BlastFurnace());
        this.converterMap.put(InventoryType.LECTERN, new CraftTileInventoryConverter.Lectern());
        this.converterMap.put(InventoryType.SMOKER, new CraftTileInventoryConverter.Smoker());
        this.converterMap.put(InventoryType.LOOM, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.CARTOGRAPHY, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.GRINDSTONE, DEFAULT_CONVERTER);
        this.converterMap.put(InventoryType.STONECUTTER, DEFAULT_CONVERTER);
    }

    public Inventory createInventory(InventoryHolder holder, InventoryType type) {
        return this.converterMap.get(type).createInventory(holder, type);
    }

    // Paper start
    public Inventory createInventory(InventoryHolder holder, InventoryType type, net.kyori.adventure.text.Component title) {
        return converterMap.get(type).createInventory(holder, type, title);
    }
    // Paper end

    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return this.converterMap.get(type).createInventory(holder, type, title);
    }

    public Inventory createInventory(InventoryHolder holder, int size) {
        return this.DEFAULT_CONVERTER.createInventory(holder, size);
    }

    // Paper start
    public Inventory createInventory(InventoryHolder holder, int size, net.kyori.adventure.text.Component title) {
        return DEFAULT_CONVERTER.createInventory(holder, size, title);
    }
    // Paper end

    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        return this.DEFAULT_CONVERTER.createInventory(holder, size, title);
    }

    public interface InventoryConverter {

        Inventory createInventory(InventoryHolder holder, InventoryType type);

        // Paper start
        Inventory createInventory(InventoryHolder holder, InventoryType type, net.kyori.adventure.text.Component title);
        // Paper end

        Inventory createInventory(InventoryHolder holder, InventoryType type, String title);
    }
}
