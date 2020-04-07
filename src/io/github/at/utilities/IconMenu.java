/*
Thanks to nisovin from bukkit forum
Modified version from https://bukkit.org/threads/icon-menu.108342/
 */

package io.github.at.utilities;

import java.util.Arrays;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class IconMenu implements Listener {

    private String name;
    private int size;
    private Plugin plugin;

    private String[] optionNames;
    private ItemStack[] optionIcons;
    private String[] optionCommands;
    private OptionClickEventHandler[] optionHandlers;

    public IconMenu(String name, int size, Plugin plugin) {
        this.name = name;
        this.size = size;
        this.plugin = plugin;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        this.optionCommands = new String[size];
        this.optionHandlers = new OptionClickEventHandler[size];
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public IconMenu setOption(int position, ItemStack icon, String name, String... info) {
        optionNames[position] = name;
        optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }

    public void setCommand(int position, String command){
        optionCommands[position] = command.startsWith("/") ? command.substring(1) : command;
    }

    public void setClickEventHandler(int position, OptionClickEventHandler handler){
        optionHandlers[position] = handler;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, size, name);
        for (int i = 0; i < optionIcons.length; i++) {
            if (optionIcons[i] != null) {
                inventory.setItem(i, optionIcons[i]);
            }
        }
        player.openInventory(inventory);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        plugin = null;
        optionNames = null;
        optionIcons = null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onInventoryClosed(InventoryCloseEvent event){
        destroy();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(name)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && optionNames[slot] != null) {
                Plugin plugin = this.plugin;
                OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, optionNames[slot]);
                final Player p = (Player)event.getWhoClicked();
                if(optionCommands[e.getPosition()] != null){
                    Bukkit.dispatchCommand(e.getPlayer(), optionCommands[e.getPosition()]);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            p.closeInventory();
                        }
                    }, 1);
                    if (e.willDestroy()) {
                        destroy();
                    }
                }else if(optionHandlers[e.getPosition()] != null){
                    optionHandlers[e.getPosition()].onOptionClick(e);
                    if (e.willClose()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            public void run() {
                                p.closeInventory();
                            }
                        }, 1);
                    }
                    if (e.willDestroy()) {
                        destroy();
                    }
                }
            }
        }
    }

    public interface OptionClickEventHandler {
        public void onOptionClick(OptionClickEvent event);
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