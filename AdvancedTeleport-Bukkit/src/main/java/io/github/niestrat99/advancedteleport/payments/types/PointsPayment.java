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
        int xpLevels = getTotalEXPFromLevel(player.getLevel());
        int totalXPinLevel = expInLevel(player.getLevel());
        return xpLevels + ((int) (player.getExp() * totalXPinLevel));
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
        int expPoints = levels == null ? 0 : getEXPBetweenLevels(player.getLevel(), (int) levels.getPaymentAmount());
        player.giveExp(-points);
        player.giveExp(-expPoints);
        if (expPoints > 0) {
            CustomMessages.sendMessage(player, "Info.paymentEXP",
                    Placeholder.unparsed("amount", String.valueOf(points + expPoints)),
                    Placeholder.unparsed("levels", String.valueOf(player.getLevel()))
            );
        }

        if (points > 0) {
            CustomMessages.sendMessage(player, "Info.paymentPoints",
                    Placeholder.unparsed("amount", String.valueOf(points + expPoints)),
                    Placeholder.unparsed("points", String.valueOf(getPlayerAmount(player)))
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
            int expPoints = getEXPBetweenLevels(player.getLevel(), (int) levels.getPaymentAmount());
            requiredPoints += expPoints;
        }
        if (getPlayerAmount(player) >= requiredPoints) {
            return true;
        } else {
            CustomMessages.sendMessage(player, "Error.notEnoughEXPPoints", Placeholder.unparsed("points", String.valueOf(requiredPoints)));

            return false;
        }
    }

    protected int getTotalEXPFromLevel(int maxLevel) {

        // Store the calculated points too.
        int expPoints = 0;

        // Next, we need to calculate the amount of EXP points to deduct.
        // Let's remove 1 off the current level each time.
        for (int i = 0; i < maxLevel; i++) {

            // Get the resulting level.
            expPoints += expInLevel(i);
        }
        return expPoints;
    }

    protected int getEXPBetweenLevels(int maxLevel, int levels) {
        int minLevel = maxLevel - levels;
        return getTotalEXPFromLevel(maxLevel) - getTotalEXPFromLevel(minLevel);
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
