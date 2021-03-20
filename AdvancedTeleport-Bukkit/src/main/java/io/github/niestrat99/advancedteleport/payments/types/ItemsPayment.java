package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.payments.Payment;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemsPayment extends Payment {

    public ItemsPayment(Material material ) {

    }

    @Override
    public double getPaymentAmount() {
        return 0;
    }

    @Override
    public void setPaymentAmount(double amount) {

    }

    @Override
    public double getPlayerAmount(Player player) {

        return 0;
    }

    @Override
    public String getMessagePath() {
        return null;
    }

    @Override
    public void setPlayerAmount(Player player) {

    }

    public static ItemsPayment getFromString(String str) {
        return null;
    }

    private boolean hasNBT(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {

        }
        return false;
    }

}
