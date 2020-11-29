package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.payments.Payment;
import org.bukkit.entity.Player;

public class PointsPayment extends Payment {

    private float points;

    public PointsPayment(float points) {
        this.points = points;
    }

    @Override
    public double getPaymentAmount() {
        return points;
    }

    @Override
    public double getPlayerAmount(Player player) {
        return player.getExp();
    }

    @Override
    public void setPlayerAmount(Player player) {
        player.setExp(player.getExp() - points);
    }
}
