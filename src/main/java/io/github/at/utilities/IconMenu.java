/*
 * Thanks to nisovin from bukkit forum
 * Modified version from https://bukkit.org/threads/icon-menu.108342/
 */

package io.github.at.utilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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
import org.bukkit.inventory.meta.SkullMeta;

public class IconMenu implements Listener, InventoryHolder {

    // Used to catch out textures which just use the hex value of the skin
    private static Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");
    // Title of the inventory
    private String title;
    // Size of the inventory, must be a multiple of 9
    private int size;
    // Current page the user is on
    private int currentPage;
    // Number of pages
    private int pageCount;

    //
    private OptionPage[] optionPages;

    private CoreClass core;
    // Stores the UUID of the player, because if we store the player itself, we start having problems
    private UUID player;
    // Stores the Inventory itself
    private Inventory inventory;

    public IconMenu(String title, int size, int pageCount, CoreClass core) {
        this.title = title;
        this.size = size;
        this.core = core;
        this.player = null;
        this.inventory = null;
        this.optionPages = new OptionPage[pageCount];
        for(int i = 0; i < optionPages.length; i++){
            optionPages[i] = new OptionPage(size);
        }
        this.currentPage = 0;
        this.pageCount = pageCount;
        core.getServer().getPluginManager().registerEvents(this, core);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
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
        updateContents();
        getPlayer().updateInventory();
    }

    private void updateContents() {
        for (int i = 0; i < size; i++) {
            Icon icon = this.optionPages[currentPage].optionIcons[i];
            if (icon != null) {
                inventory.setItem(i, icon.item);
            }else {
                inventory.clear(i);
            }
        }
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        core = null;
        optionPages = null;
        player = null;
        inventory = null;
    }

    public int getPageCount() {
        return pageCount;
    }

    private static class OptionPage {
        private Icon[] optionIcons;

        public OptionPage(int size){
            this.optionIcons = new Icon[size];
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onInventoryClosed(InventoryCloseEvent event){
        destroy();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        // Checking if the holder is an instance of IconMenu to prevent potential conflict title comparison can cause.
        if (event.getInventory().getHolder() == this) {
            // Cancel the event, stopping the player pick up the item.
            event.setCancelled(true);
            // Get the raw slot (NOT the slot)
            int slot = event.getRawSlot();
            OptionPage currentPage = this.optionPages[this.currentPage];
            // Make sure the slot is inside the custom inventory, and that the icon clicked isn't null
            if (slot >= 0 && slot < size && currentPage.optionIcons[slot] != null) {
                CoreClass plugin = this.core;
                Icon icon = currentPage.optionIcons[slot];
                OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, icon);
                final Player p = (Player)event.getWhoClicked();
                icon.activate(p, e);
                if (e.willClose()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, p::closeInventory, 1);
                }
                if (e.willDestroy()) {
                    destroy();
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

    public static class Icon {

        private ItemStack item;
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
            String typeName = item.getType().name();
            // Make sure it's an actual skull and the texture exists - otherwise, skip it all
            if (((typeName.equalsIgnoreCase("SKULL_ITEM") && item.getDurability() == 3)
                    || typeName.equalsIgnoreCase("PLAYER_HEAD"))
                    && texture != null
                    && !texture.isEmpty()) {
                // Create the fake game profile - it needs a UUID, otherwise there would be a delay in setting the texture.
                GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "AdvTPHead");
                // The data which contains the texture.
                byte[] data;
                // If the texture provided is the URL:
                if (texture.startsWith("http")) {
                    // Just stick it into the JSON format.
                    data = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", texture).getBytes());
                // However, if the hex pattern after the URL is used instead:
                } else if (HEX_PATTERN.matcher(texture).matches()) {
                    // Just add the extra bit in front.
                    data = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/%s\"}}}", texture).getBytes());
                } else {
                    // But if it isn't the raw texture, it's likely to already be Base64 encoded. So don't worry about that too much.
                    data = texture.getBytes();
                }
                // Set the texture
                profile.getProperties().put("textures", new Property("textures", new String(data)));

                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

                try {
                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                item.setItemMeta(skullMeta);
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

    public class OptionClickEvent {
        private Player player;
        private int position;
        private Icon icon;
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
}

