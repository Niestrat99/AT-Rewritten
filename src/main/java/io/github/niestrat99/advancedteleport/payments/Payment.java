package io.github.niestrat99.advancedteleport.payments;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import org.bukkit.entity.Player;

public abstract class Payment {

    public abstract double getPaymentAmount();
    public abstract double getPlayerAmount(Player player);
    public abstract String getMessagePath();

    public abstract void setPlayerAmount(Player player);

    public boolean canPay(Player player) {
        if (getPaymentAmount() == 0) return true;
        return getPlayerAmount(player) >= getPaymentAmount();
    }

    public boolean withdraw(Player player) {
        if (canPay(player)) {
            setPlayerAmount(player);
            player.sendMessage(
                    CustomMessages.getString(getMessagePath())
                            .replaceAll("\\{amount}", String.valueOf(getPaymentAmount()))
                            .replaceAll("\\{levels}", String.valueOf(player.getLevel())));
            return true;
        }
        return false;
    }
}
