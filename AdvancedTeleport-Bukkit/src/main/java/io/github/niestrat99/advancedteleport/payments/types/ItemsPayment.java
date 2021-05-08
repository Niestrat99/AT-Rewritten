package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.payments.Payment;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemsPayment extends Payment {

    private Material material;
    private int amount;
    private byte data;

    // DIAMOND#{Count:10,tag:{display:{Name:"&b&lSetwarp Token"}}}
    public ItemsPayment(Material material, byte data, int amount) {
        this.material = material;
        this.amount = amount;
        this.data = data;
    }

    @Override
    public double getPaymentAmount() {
        return amount;
    }

    @Override
    public void setPaymentAmount(double amount) {
        this.amount = (int) amount;
    }

    @Override
    public double getPlayerAmount(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory()) {
            if (item.getType() == material && item.getDurability() == data) {
                count += item.getAmount();
            }
        }
        return count;
    }

    @Override
    public String getMessagePath() {
        return null;
    }

    @Override
    public void setPlayerAmount(Player player) {
        int remaining = amount;
        for (ItemStack item : player.getInventory()) {

        }
    }

    public Material getMaterial() {
        return material;
    }

    public static ItemsPayment getFromString(String str) {
        String[] parts = str.split(":");

        switch (parts.length) {
            case 0:
                return null;
            case 1:
                Material material = Material.getMaterial(parts[0]);
                if (material == null) return null;
                return new ItemsPayment(material, (byte) 0, 1);
            case 2:
                material = Material.getMaterial(parts[0]);
                if (material == null) return null;
                int amount = 1;
                if (parts[1].matches("^[0-9]+$")) {
                    amount = Integer.parseInt(parts[1]);
                }
                return new ItemsPayment(material, (byte) 0, amount);
            default:
                material = Material.getMaterial(parts[0]);
                if (material == null) return null;
                amount = 1;
                if (parts[2].matches("^[0-9]+$")) {
                    amount = Integer.parseInt(parts[2]);
                }
                byte data = 0;
                if (parts[1].matches("^[0-9]+$")) {
                    data = Byte.parseByte(parts[1]);
                }
                return new ItemsPayment(material, data, amount);
        }
    }

    // TODO: Implement NBT
    private boolean hasNBT(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
        }
        return false;
    }

    public class NBTJSONRepresentation {

    }
}
