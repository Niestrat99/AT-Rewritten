/*
 * Thanks to nisovin from bukkit forum
 * Modified version from https://bukkit.org/threads/icon-menu.108342/
 */

package io.github.niestrat99.advancedteleport.utilities;

import io.github.niestrat99.advancedteleport.CoreAdvancedTeleport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class IconMenu implements Listener, InventoryHolder {

    // Used to catch out textures which just use the hex value of the skin
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");
    // Title of the inventory
    private final String title;
    // Size of the inventory, must be a multiple of 9
    private final int size;
    // Number of pages
    private final int pageCount;
    // Current page the user is on
    private int currentPage;
    //
    private OptionPage[] optionPages;

    private Plugin core;
    // Stores the UUID of the player, because if we store the player itself, we start having
    // problems
    private UUID player;
    // Stores the Inventory itself
    private Inventory inventory;

    public IconMenu(String title, int size, int pageCount, Plugin core) {
        this.title = title;
        this.size = size;
        this.core = core;
        this.player = null;
        this.inventory = null;
        this.optionPages = new OptionPage[pageCount];
        for (int i = 0; i < optionPages.length; i++) {
            optionPages[i] = new OptionPage(size);
        }
        this.currentPage = 0;
        this.pageCount = pageCount;
        core.getServer().getPluginManager().registerEvents(this, core);
    }

    private static ItemStack setItemNameAndLore(ItemStack item, String name, List<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> colLore = new ArrayList<>();
        for (String str : lore) {
            colLore.add(ChatColor.translateAlternateColorCodes('&', str));
        }
        im.setLore(colLore);
        item.setItemMeta(im);
        return item;
    }

    public IconMenu setIcon(int page, int position, Icon icon) {
        this.optionPages[page].optionIcons[position] = icon;
        return this;
    }

    public void open(Player player) {
        this.player = player.getUniqueId();
        inventory = Bukkit.createInventory(this, size, title);
        updateContents();
        player.openInventory(inventory);
    }

    private void updateContents() {
        for (int i = 0; i < size; i++) {
            Icon icon = this.optionPages[currentPage].optionIcons[i];
            if (icon != null) {
                inventory.setItem(i, icon.item);
            } else {
                inventory.clear(i);
            }
        }
    }

    public void openNextPage() {
        if (this.currentPage + 1 >= pageCount) return;
        this.currentPage++;
        this.updatePage();
    }

    public void updatePage() {
        updateContents();
        getPlayer().updateInventory();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public void openPreviousPage() {
        if (this.currentPage - 1 < 0) return;
        this.currentPage--;
        this.updatePage();
    }

    public void openPage(int page) {
        if (page >= pageCount || page < 0) return;
        this.currentPage = page;
        this.updatePage();
    }

    public int getPageCount() {
        return pageCount;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClosed(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() == this) {
            destroy();
        }
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        core = null;
        optionPages = null;
        player = null;
        inventory = null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        // Checking if the holder is an instance of IconMenu to prevent potential conflict title
        // comparison can cause.
        if (event.getInventory().getHolder() != this) return;
        // Cancel the event, stopping the player pick up the item.
        event.setCancelled(true);
        // Get the raw slot (NOT the slot)
        int slot = event.getRawSlot();
        OptionPage currentPage = this.optionPages[this.currentPage];
        // Make sure the slot is inside the custom inventory, and that the icon clicked isn't null
        if (slot >= 0 && slot < size && currentPage.optionIcons[slot] != null) {
            Plugin plugin = this.core;
            Icon icon = currentPage.optionIcons[slot];
            OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, icon);
            final Player p = (Player) event.getWhoClicked();
            icon.activate(p, e);
            if (e.willClose()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, p::closeInventory, 1);
            }
            if (e.willDestroy()) {
                destroy();
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public interface OptionClickEventHandler {
        void onOptionClick(OptionClickEvent event);
    }

    private static class OptionPage {
        private final Icon[] optionIcons;

        public OptionPage(int size) {
            this.optionIcons = new Icon[size];
        }
    }

    public static class Icon {

        private final ItemStack item;
        private String[] commands;
        private OptionClickEventHandler handler;

        public Icon(ItemStack item) {
            this.item = item;
        }

        public Icon withCommands(String... commands) {
            this.commands = commands;
            return this;
        }

        public Icon withNameAndLore(String name, List<String> lore) {
            setItemNameAndLore(item, name, lore);
            return this;
        }

        public Icon withTexture(String texture) {
            if (item.getType() == Material.PLAYER_HEAD
                    && texture != null
                    && !texture.isEmpty()) {
                try {
                    CoreAdvancedTeleport.getInstance().setTexture(item, texture);
                } catch (MalformedURLException e) {
                    CoreAdvancedTeleport.getInstance().getPlugin().getLogger().warning("Failed to parse " + texture + " into a valid head texture.");
                }
            }
            return this;
        }

        public Icon withHandler(OptionClickEventHandler handler) {
            this.handler = handler;
            return this;
        }

        public void activate(Player player, OptionClickEvent event) {
            if (commands != null) {
                for (String command : commands) {
                    Bukkit.dispatchCommand(player, command);
                }
            }
            if (handler != null) {
                handler.onOptionClick(event);
            }
        }

        public ItemStack getItem() {
            return item;
        }
    }

    public static class OptionClickEvent {
        private final Player player;
        private final int position;
        private final Icon icon;
        private boolean close;
        private boolean destroy;

        public OptionClickEvent(Player player, int position, Icon icon) {
            this.player = player;
            this.position = position;
            this.icon = icon;
            this.close = true;
            this.destroy = true;
        }

        public Player getPlayer() {
            return player;
        }

        public int getPosition() {
            return position;
        }

        public Icon getIcon() {
            return icon;
        }

        public boolean willClose() {
            return close;
        }

        public boolean willDestroy() {
            return destroy;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }
    }
}
