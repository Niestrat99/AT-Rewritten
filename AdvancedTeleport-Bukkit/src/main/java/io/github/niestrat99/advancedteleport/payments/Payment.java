package io.github.niestrat99.advancedteleport.payments;

import org.bukkit.entity.Player;

public abstract class Payment {

    public abstract double getPaymentAmount();
    public abstract void setPaymentAmount(double amount);
    public abstract double getPlayerAmount(Player player);
    public abstract String getMessagePath();
    public abstract String getId();

    public abstract void setPlayerAmount(Player player);

    public boolean canPay(Player player) {
        if (getPaymentAmount() == 0) return true;
        return getPlayerAmount(player) >= getPaymentAmount();
    }

    public boolean withdraw(Player player) {
        if (canPay(player)) {
            setPlayerAmount(player);
            return true;
        }
        return false;
    }
}
