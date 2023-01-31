package io.github.niestrat99.advancedteleport.payments;

import org.bukkit.entity.Player;

/**
 * Used to represent a form of payment that may be made when the player teleports.
 */
public abstract class Payment {

    /**
     * The path in the custommessages.yml file to the error message that is returned if the player cannot pay for the teleportation.
     *
     * @return the error message path in custommessages.yml.
     */
    // TODO - needs usage rather than internal message handling
    public abstract String getMessagePath();

    /**
     * Gets the ID of the payment type to be made.
     *
     * @return the ID of the payment type.
     */
    public abstract String getId();

    /**
     * Withdraws the cost from the player.
     *
     * @param player the player paying.
     * @return true if the payment was done successfully, false if it was not (e.g. if they couldn't afford to pay it).
     */
    public boolean withdraw(Player player) {
        if (canPay(player)) {
            setPlayerAmount(player);
            return true;
        }
        return false;
    }

    /**
     * Whether the player can afford the payment type.
     *
     * @param player the player paying.
     * @return true if the player can pay, false if they cannot.
     */
    public boolean canPay(Player player) {
        if (getPaymentAmount() == 0) return true;
        return getPlayerAmount(player) >= getPaymentAmount();
    }

    /**
     * Withdraws the payment from the player.
     *
     * @param player the player paying.
     */
    public abstract void setPlayerAmount(Player player);

    /**
     * Gets the value of the payment to make.
     *
     * @return the value of the payment to make in a double form.
     */
    public abstract double getPaymentAmount();

    /**
     * Sets the value of the payment to make.
     *
     * @param amount the value of the payment to make in a double form.
     */
    public abstract void setPaymentAmount(double amount);

    /**
     * Gets the value of the payment type that the player currently has.
     *
     * @param player the player to check.
     * @return the value of the payment type that the player has.
     */
    public abstract double getPlayerAmount(Player player);
}
