package org.bukkit.craftbukkit.v1_19_R1.inventory;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class CraftInventoryView extends InventoryView {
    private final AbstractContainerMenu container;
    private final CraftHumanEntity player;
    private final CraftInventory viewing;

    public CraftInventoryView(HumanEntity player, Inventory viewing, AbstractContainerMenu container) {
        // TODO: Should we make sure it really IS a CraftHumanEntity first? And a CraftInventory?
        this.player = (CraftHumanEntity) player;
        this.viewing = (CraftInventory) viewing;
        this.container = container;
    }

    @Override
    public Inventory getTopInventory() {
        return this.viewing;
    }

    @Override
    public Inventory getBottomInventory() {
        return this.player.getInventory();
    }

    @Override
    public HumanEntity getPlayer() {
        return this.player;
    }

    @Override
    public InventoryType getType() {
        InventoryType type = this.viewing.getType();
        if (type == InventoryType.CRAFTING && this.player.getGameMode() == GameMode.CREATIVE) {
            return InventoryType.CREATIVE;
        }
        return type;
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
        if (slot >= 0) {
            this.container.getSlot(slot).set(stack);
        } else {
            this.player.getHandle().drop(stack, false);
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0) {
            return null;
        }
        return CraftItemStack.asCraftMirror(this.container.getSlot(slot).getItem());
    }

    // Paper start
    @Override
    public net.kyori.adventure.text.Component title() {
        return io.papermc.paper.adventure.PaperAdventure.asAdventure(this.container.getTitle());
    }
    // Paper end

    @Override
    public String getTitle() {
        return CraftChatMessage.fromComponent(this.container.getTitle());
    }

    public boolean isInTop(int rawSlot) {
        return rawSlot < this.viewing.getSize();
    }

    public AbstractContainerMenu getHandle() {
        return this.container;
    }
}
