package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.payments.Payment;
import org.bukkit.entity.Player;

public class PointsPayment extends Payment {

    private int points;
    private LevelsPayment levels;

    public PointsPayment(int points) {
        this.points = points;
        this.levels = null;
    }

    @Override
    public double getPaymentAmount() {
        return points;
    }

    @Override
    public void setPaymentAmount(double amount) {
        points = (int) amount;
    }

    public void addLevels(LevelsPayment levels) {
        if (this.levels == null) {
            this.levels = levels;
        } else {
            this.levels.setPaymentAmount(this.levels.getPaymentAmount() + levels.getPaymentAmount());
        }
    }

    @Override
    public double getPlayerAmount(Player player) {

        // Get the XP that the player is on according to their level
        int xpLevels = getEXPBetweenLevels(player.getLevel());
        int totalXPinLevel = expInLevel(player.getLevel());
        return xpLevels + totalXPinLevel - player.getExpToLevel();
    }

    @Override
    public boolean canPay(Player player) {
        int requiredPoints = points;
        if (levels != null) {
            if (levels.getPaymentAmount() > player.getLevel()) {
                CustomMessages.sendMessage(player, "Error.notEnoughEXP", "{levels}", String.valueOf(levels.getPaymentAmount()));
                return false;
            }
            int expPoints = getEXPBetweenLevels(player.getLevel());
            requiredPoints += expPoints;
        }
        if (getPlayerAmount(player) >= requiredPoints) {
            return true;
        } else {
            CustomMessages.sendMessage(player, "Error.notEnoughEXPPoints", "{points}", String.valueOf(requiredPoints));

            return false;
        }
    }

    @Override
    public String getMessagePath() {
        return "Info.paymentPoints";
    }

    @Override
    public void setPlayerAmount(Player player) {
        int expPoints = getEXPBetweenLevels(player.getLevel());
        player.giveExp(-points);
        player.giveExp(-expPoints);
        if (expPoints > 0) {
            CustomMessages.sendMessage(player, "Info.paymentEXP",
                    "{amount}", String.valueOf(levels.getPaymentAmount()),
                    "{levels}", String.valueOf(player.getLevel()));
        }
    }

    protected int getEXPBetweenLevels(int startingLevel) {
        // Store the calculated points too.
        int expPoints = 0;
        // If there's no levels though, return 0.
        if (levels == null) return 0;
        // Next, we need to calculate the amount of EXP points to deduct.
        // Let's remove 1 off the current level each time.
        for (int i = 0; i < levels.getPaymentAmount(); i++) {
            expPoints += expInLevel(startingLevel - i - 1);
        }
        return expPoints;
    }

    protected int expInLevel(int currentLevel) {
        if (currentLevel < 16) {
            return 2 * currentLevel + 7;
        } else if (currentLevel < 31) {
            return 5 * currentLevel - 38;
        } else {
            return 9 * currentLevel - 158;
        }
    }
}
