package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.payments.Payment;
import org.bukkit.entity.Player;

public class VaultPayment extends Payment {

    private double price;

    public VaultPayment(double price) {
        this.price = price;
    }

    @Override
    public double getPaymentAmount() {
        return price;
    }

    @Override
    public double getPlayerAmount(Player player) {
        return 0;
    }

    @Override
    public void setPlayerAmount(Player player) {

    }
}
