/*
 * Thanks to nisovin from bukkit forum
 * Modified version from https://bukkit.org/threads/icon-menu.108342/
 */

package io.github.at.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.github.at.main.CoreClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class IconMenu implements Listener, InventoryHolder {

    private String name;
    private int size;
    private int currentPage;
    private int pageCount;

    private OptionPage[] optionPages;

    private Plugin plugin;
    private Player player;
    private Inventory inventory;

    public IconMenu(String name, int size, int pageCount, Plugin plugin) {
        this.name = name;
        this.size = size;
        this.plugin = plugin;
        this.player = null;
        this.inventory = null;
        this.optionPages = new OptionPage[pageCount];
        for(int i = 0; i < optionPages.length; i++){
            optionPages[i] = new OptionPage(size);
        }
        this.currentPage = 0;
        this.pageCount = pageCount;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public IconMenu setOption(int page, int position, ItemStack icon, String name, String... info) {
        this.optionPages[page].optionNames[position] = name;
        this.optionPages[page].optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }

    public void setCommand(int page, int position, String command){
        this.optionPages[page].optionCommands[position] = command.startsWith("/") ? command.substring(1) : command;
    }

    public void setClickEventHandler(int page, int position, OptionClickEventHandler handler){
        this.optionPages[page].optionHandlers[position] = handler;
    }

    public void open(Player player) {
        this.player = player;
        inventory = Bukkit.createInventory(player, size, name);
        for (int i = 0; i < size; i++) {
            if (this.optionPages[currentPage].optionIcons[i] != null) {
                inventory.setItem(i, this.optionPages[currentPage].optionIcons[i]);
            }
        }
        this.player.openInventory(inventory);
    }

    public void openNextPage() {
        if (this.currentPage+1 >= pageCount) return;
        this.currentPage++;
        this.updatePage();
    }

    public void openPreviousPage() {
        if (this.currentPage-1 < 0) return;
        this.currentPage--;
        this.updatePage();
    }

    public void openPage(int page) {
        if (page >= pageCount || page < 0) return;
        this.currentPage = page;
        this.updatePage();
    }

    public void updatePage() {
        for (int i = 0; i < size; i++) {
            if (this.optionPages[currentPage].optionIcons[i] != null) {
                inventory.setItem(i, this.optionPages[currentPage].optionIcons[i]);
            }else {
                inventory.clear(i);
            }
        }
        this.player.updateInventory();
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        plugin = null;
        optionPages = null;
        player = null;
        inventory = null;
    }

    private static class OptionPage {
        private String[] optionNames;
        private ItemStack[] optionIcons;
        private String[] optionCommands;
        private OptionClickEventHandler[] optionHandlers;

        public OptionPage(int size){
            this.optionNames = new String[size];
            this.optionIcons = new ItemStack[size];
            this.optionCommands = new String[size];
            this.optionHandlers = new OptionClickEventHandler[size];
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onInventoryClosed(InventoryCloseEvent event){
        destroy();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof IconMenu) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && this.optionPages[currentPage].optionNames[slot] != null) {
                Plugin plugin = this.plugin;
                OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, this.optionPages[currentPage].optionNames[slot]);
                final Player p = (Player)event.getWhoClicked();
                if(this.optionPages[currentPage].optionCommands[e.getPosition()] != null){
                    Bukkit.dispatchCommand(e.getPlayer(), this.optionPages[currentPage].optionCommands[e.getPosition()]);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            p.closeInventory();
                        }
                    }, 1);
                    if (e.willDestroy()) {
                        destroy();
                    }
                }else if(this.optionPages[currentPage].optionHandlers[e.getPosition()] != null){
                    this.optionPages[currentPage].optionHandlers[e.getPosition()].onOptionClick(e);
                    if (e.willClose()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, p::closeInventory, 1);
                    }
                    if (e.willDestroy()) {
                        destroy();
                    }
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public interface OptionClickEventHandler {
        public void onOptionClick(OptionClickEvent event);
    }

    public class Icon {

        private ItemStack item;
        private b
        private String[] commands;
        private String name;
        private OptionClickEventHandler handler;

        public Icon(ItemStack item) {
            this.item = item;
        }
    }

    public class OptionClickEvent {
        private Player player;
        private int position;
        private String name;
        private boolean close;
        private boolean destroy;

        public OptionClickEvent(Player player, int position, String name) {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = true;
            this.destroy = true;
        }

        public Player getPlayer() {
            return player;
        }

        public int getPosition() {
            return position;
        }

        public String getName() {
            return name;
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

    private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }
}

