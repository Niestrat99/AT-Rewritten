package com.destroystokyo.paper.loottable;

import io.papermc.paper.configuration.WorldConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTable;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PaperLootableInventoryData {

    private static final Random RANDOM = new Random();

    private long lastFill = -1;
    private long nextRefill = -1;
    private int numRefills = 0;
    private Map<UUID, Long> lootedPlayers;
    private final PaperLootableInventory lootable;

    public PaperLootableInventoryData(PaperLootableInventory lootable) {
        this.lootable = lootable;
    }

    long getLastFill() {
        return this.lastFill;
    }

    long getNextRefill() {
        return this.nextRefill;
    }

    long setNextRefill(long nextRefill) {
        long prev = this.nextRefill;
        this.nextRefill = nextRefill;
        return prev;
    }

    public boolean shouldReplenish(@Nullable net.minecraft.world.entity.player.Player player) {
        LootTable table = this.lootable.getLootTable();

        // No Loot Table associated
        if (table == null) {
            return false;
        }

        // ALWAYS process the first fill or if the feature is disabled
        if (this.lastFill == -1 || !this.lootable.getNMSWorld().paperConfig().lootables.autoReplenish) {
            return true;
        }

        // Only process refills when a player is set
        if (player == null) {
            return false;
        }

        // Chest is not scheduled for refill
        if (this.nextRefill == -1) {
            return false;
        }

        final WorldConfiguration paperConfig = this.lootable.getNMSWorld().paperConfig();

        // Check if max refills has been hit
        if (paperConfig.lootables.maxRefills != -1 && this.numRefills >= paperConfig.lootables.maxRefills) {
            return false;
        }

        // Refill has not been reached
        if (this.nextRefill > System.currentTimeMillis()) {
            return false;
        }


        final Player bukkitPlayer = (Player) player.getBukkitEntity();
        LootableInventoryReplenishEvent event = new LootableInventoryReplenishEvent(bukkitPlayer, lootable.getAPILootableInventory());
        if (paperConfig.lootables.restrictPlayerReloot && hasPlayerLooted(player.getUUID())) {
            event.setCancelled(true);
        }
        return event.callEvent();
    }
    public void processRefill(@Nullable net.minecraft.world.entity.player.Player player) {
        this.lastFill = System.currentTimeMillis();
        final WorldConfiguration paperConfig = this.lootable.getNMSWorld().paperConfig();
        if (paperConfig.lootables.autoReplenish) {
            long min = paperConfig.lootables.refreshMin.seconds();
            long max = paperConfig.lootables.refreshMax.seconds();
            this.nextRefill = this.lastFill + (min + RANDOM.nextLong(max - min + 1)) * 1000L;
            this.numRefills++;
            if (paperConfig.lootables.resetSeedOnFill) {
                this.lootable.setSeed(0);
            }
            if (player != null) { // This means that numRefills can be incremented without a player being in the lootedPlayers list - Seems to be EntityMinecartChest specific
                this.setPlayerLootedState(player.getUUID(), true);
            }
        } else {
            this.lootable.clearLootTable();
        }
    }


    public void loadNbt(CompoundTag base) {
        if (!base.contains("Paper.LootableData", 10)) { // 10 = compound
            return;
        }
        CompoundTag comp = base.getCompound("Paper.LootableData");
        if (comp.contains("lastFill")) {
            this.lastFill = comp.getLong("lastFill");
        }
        if (comp.contains("nextRefill")) {
            this.nextRefill = comp.getLong("nextRefill");
        }

        if (comp.contains("numRefills")) {
            this.numRefills = comp.getInt("numRefills");
        }
        if (comp.contains("lootedPlayers", 9)) { // 9 = list
            ListTag list = comp.getList("lootedPlayers", 10); // 10 = compound
            final int size = list.size();
            if (size > 0) {
                this.lootedPlayers = new HashMap<>(list.size());
            }
            for (int i = 0; i < size; i++) {
                final CompoundTag cmp = list.getCompound(i);
                lootedPlayers.put(cmp.getUUID("UUID"), cmp.getLong("Time"));
            }
        }
    }
    public void saveNbt(CompoundTag base) {
        CompoundTag comp = new CompoundTag();
        if (this.nextRefill != -1) {
            comp.putLong("nextRefill", this.nextRefill);
        }
        if (this.lastFill != -1) {
            comp.putLong("lastFill", this.lastFill);
        }
        if (this.numRefills != 0) {
            comp.putInt("numRefills", this.numRefills);
        }
        if (this.lootedPlayers != null && !this.lootedPlayers.isEmpty()) {
            ListTag list = new ListTag();
            for (Map.Entry<UUID, Long> entry : this.lootedPlayers.entrySet()) {
                CompoundTag cmp = new CompoundTag();
                cmp.putUUID("UUID", entry.getKey());
                cmp.putLong("Time", entry.getValue());
                list.add(cmp);
            }
            comp.put("lootedPlayers", list);
        }

        if (!comp.isEmpty()) {
            base.put("Paper.LootableData", comp);
        }
    }

    void setPlayerLootedState(UUID player, boolean looted) {
        if (looted && this.lootedPlayers == null) {
            this.lootedPlayers = new HashMap<>();
        }
        if (looted) {
            if (!this.lootedPlayers.containsKey(player)) {
                this.lootedPlayers.put(player, System.currentTimeMillis());
            }
        } else if (this.lootedPlayers != null) {
            this.lootedPlayers.remove(player);
        }
    }

    boolean hasPlayerLooted(UUID player) {
        return this.lootedPlayers != null && this.lootedPlayers.containsKey(player);
    }

    Long getLastLooted(UUID player) {
        return lootedPlayers != null ? lootedPlayers.get(player) : null;
    }
}
