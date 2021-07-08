package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.payments.Payment;
import org.bukkit.entity.Player;

public class LevelsPayment extends Payment {

    private int levels;

    public LevelsPayment(int levels) {
        this.levels = levels;
    }

    @Override
    public double getPaymentAmount() {
        return levels;
    }

    @Override
    public void setPaymentAmount(double amount) {
        this.levels = (int) amount;
    }

    @Override
    public double getPlayerAmount(Player player) {
        return player.getLevel();
    }

    @Override
    public String getMessagePath() {
        return "Info.paymentEXP";
    }

    @Override
    public void setPlayerAmount(Player player) {
    }


}
