package io.github.niestrat99.advancedteleport.payments;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.payments.types.LevelsPayment;
import io.github.niestrat99.advancedteleport.payments.types.PointsPayment;
import io.github.niestrat99.advancedteleport.payments.types.VaultPayment;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PaymentManager {

    private HashMap<String, HashMap<String, Payment>> teleportCosts;

    private static PaymentManager instance;

    public PaymentManager() {
        instance = this;
        teleportCosts = new HashMap<>();
        addCommand("tpa", NewConfig.get().COSTS.TPA.get());
        addCommand("tpahere", NewConfig.get().COSTS.TPAHERE.get());
        addCommand("tpr", NewConfig.get().COSTS.TPR.get());
        addCommand("spawn", NewConfig.get().COSTS.SPAWN.get());
        addCommand("warp", NewConfig.get().COSTS.WARP.get());
        addCommand("home", NewConfig.get().COSTS.HOME.get());
        addCommand("back", NewConfig.get().COSTS.BACK.get());
    }

    private void addCommand(String command, Object value) {
        HashMap<String, Payment> payments = new HashMap<>();
        String[] rawPayments = String.valueOf(value).split(";");
        for (String rawPayment : rawPayments) {
            try {
                if (rawPayment.length() - 3 <= 0) {
                    if (CoreClass.getVault() != null) {
                        addPayment("vault", new VaultPayment(Double.parseDouble(rawPayment)), payments);
                    }
                    continue;
                }
                String points = rawPayment.substring(0, rawPayment.length() - 3);
                if (rawPayment.endsWith("LVL")) {
                    addPayment("levels", new LevelsPayment(Integer.parseInt(points)), payments);
                } else if (rawPayment.endsWith("EXP")) {
                    addPayment("exp", new PointsPayment(Integer.parseInt(points)), payments);
                } else {
                    if (CoreClass.getVault() != null) {
                        addPayment("vault", new VaultPayment(Double.parseDouble(rawPayment)), payments);
                    }
                }
            } catch (Exception e) {
                CoreClass.getInstance().getLogger().warning("Failed to parse payment " + rawPayment + " for command " + command + "!");
            }
        }
        teleportCosts.put(command, payments);
    }

    private void addPayment(String type, Payment payment, HashMap<String, Payment> currentPayMethods) {
        if (type.equalsIgnoreCase("levels")) {
            if (currentPayMethods.containsKey("exp")) {
                PointsPayment existingPayment = (PointsPayment) currentPayMethods.get("exp");
                existingPayment.addLevels((LevelsPayment) payment);
            } else {
                PointsPayment newPayment = new PointsPayment(0);
                newPayment.addLevels((LevelsPayment) payment);
                currentPayMethods.put("exp", newPayment);
            }
        } else {
            if (currentPayMethods.containsKey(type)) {
                Payment existingPayment = currentPayMethods.get(type);
                existingPayment.setPaymentAmount(payment.getPaymentAmount() + existingPayment.getPaymentAmount());
            } else {
                currentPayMethods.put(type, payment);
            }
        }

    }

    // Method used to check if a player can pay for using a command
    public boolean canPay(String command, Player player) {
        if (player.hasPermission("at.admin.bypass.payment")) return true;
        for (Payment payment : teleportCosts.get(command).values()) {
            if (!payment.canPay(player)) {
                return false;
            }
        }
        return true;
    }

    // Method used to manage payments
    public void withdraw(String command, Player player) {
        if (!player.hasPermission("at.admin.bypass")) {
            for (Payment payment : teleportCosts.get(command).values()) {
                payment.withdraw(player);
            }
        }
    }

    public static PaymentManager getInstance() {
        return instance;
    }
}
