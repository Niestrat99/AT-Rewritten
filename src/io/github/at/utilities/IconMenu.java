/*
Thanks to nisovin from bukkit forum
Modified version from https://bukkit.org/threads/icon-menu.108342/
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class IconMenu implements Listener, InventoryHolder {

    private String title; // Title of the inventory
    private int size; // Size of inventory (must be a multiple of 9)
    private CoreClass core;

    private HashMap<Integer, Icon> icons;
    private String[] optionNames;
    private ItemStack[] optionIcons;
    private String[] optionCommands;
    private OptionClickEventHandler[] optionHandlers;

    public IconMenu(String title, int size, CoreClass core) {
        this.title = title;
        this.size = size;
        this.core = core;
        this.icons = new HashMap<>();
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        this.optionCommands = new String[size];
        this.optionHandlers = new OptionClickEventHandler[size];
        core.getServer().getPluginManager().registerEvents(this, core);
    }

    public IconMenu setOption(int position, ItemStack item, String name, String command, String... info) {
        item = setItemNameAndLore(item, name, info);
        Icon icon = new Icon(item);
        optionNames[position] = name;
        optionCommands[position] = command.startsWith("/") ? command.substring(1) : command;
        optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }

    public IconMenu setIcon(int position, Icon icon)

    public IconMenu setOption(int position, ItemStack icon, String name, OptionClickEventHandler handler, String... info) {
        optionNames[position] = name;
        optionHandlers[position] = handler;
        optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(this, size, title);
        for (int i = 0; i < optionIcons.length; i++) {
            if (optionIcons[i] != null) {
                inventory.setItem(i, optionIcons[i]);
            }
        }
        player.openInventory(inventory);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        core = null;
        optionNames = null;
        optionIcons = null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof IconMenu) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && optionNames[slot] != null) {
                Plugin plugin = this.core;
                OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, optionNames[slot]);

                final Player p = (Player)event.getWhoClicked();

                if (optionCommands[e.getPosition()] != null){
                    Bukkit.dispatchCommand(e.getPlayer(), optionCommands[e.getPosition()]);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, p::closeInventory, 1);
                    destroy();
                }else{
                    optionHandlers[e.getPosition()].onOptionClick(e);
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