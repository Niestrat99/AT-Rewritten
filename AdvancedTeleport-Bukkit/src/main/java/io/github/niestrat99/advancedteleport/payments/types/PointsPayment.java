package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.payments.Payment;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

    @Override
    public double getPlayerAmount(Player player) {

        // Get the XP that the player is on according to their level
        int xpLevels = getEXPBetweenLevels(player.getLevel());
        int totalXPinLevel = expInLevel(player.getLevel());
        return xpLevels + totalXPinLevel - player.getExpToLevel();
    }

    @Override
    public String getMessagePath() {
        return "Info.paymentPoints";
    }

    @Override
    public String getId() {
        return "exp";
    }

    @Override
    public void setPlayerAmount(Player player) {
        int expPoints = getEXPBetweenLevels(player.getLevel());
        player.giveExp(-points);
        player.giveExp(-expPoints);
        if (expPoints > 0) {
            CustomMessages.sendMessage(player, "Info.paymentEXP",
                    Placeholder.unparsed("amount", String.valueOf(levels.getPaymentAmount())),
                    Placeholder.unparsed("levels", String.valueOf(player.getLevel()))
            );
        }
    }

    @Override
    public boolean canPay(Player player) {
        int requiredPoints = points;
        if (levels != null) {
            if (levels.getPaymentAmount() > player.getLevel()) {
                CustomMessages.sendMessage(player, "Error.notEnoughEXP", Placeholder.unparsed("levels", String.valueOf(levels.getPaymentAmount())));
                return false;
            }
            int expPoints = getEXPBetweenLevels(player.getLevel());
            requiredPoints += expPoints;
        }
        if (getPlayerAmount(player) >= requiredPoints) {
            return true;
        } else {
            CustomMessages.sendMessage(player, "Error.notEnoughEXPPoints", Placeholder.unparsed("points", String.valueOf(requiredPoints)));

            return false;
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
            // Get the resulting level.
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

    public void addLevels(LevelsPayment levels) {
        if (this.levels == null) {
            this.levels = levels;
        } else {
            this.levels.setPaymentAmount(this.levels.getPaymentAmount() + levels.getPaymentAmount());
        }
    }
}
