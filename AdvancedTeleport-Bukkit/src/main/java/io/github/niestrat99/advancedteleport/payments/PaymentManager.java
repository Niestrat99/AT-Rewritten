package io.github.niestrat99.advancedteleport.payments;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.MainConfig;
import io.github.niestrat99.advancedteleport.payments.types.ItemsPayment;
import io.github.niestrat99.advancedteleport.payments.types.LevelsPayment;
import io.github.niestrat99.advancedteleport.payments.types.PointsPayment;
import io.github.niestrat99.advancedteleport.payments.types.VaultPayment;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentManager {

    private static PaymentManager instance;
    private final HashMap<String, HashMap<String, Payment>> teleportCosts;

    public PaymentManager() {
        instance = this;
        teleportCosts = new HashMap<>();

        // Registers the payments for each command.
        addCommand("tpa", MainConfig.get().COSTS.TPA.get());
        addCommand("tpahere", MainConfig.get().COSTS.TPAHERE.get());
        addCommand("tpr", MainConfig.get().COSTS.TPR.get());
        addCommand("spawn", MainConfig.get().COSTS.SPAWN.get());
        addCommand("warp", MainConfig.get().COSTS.WARP.get());
        addCommand("home", MainConfig.get().COSTS.HOME.get());
        addCommand("back", MainConfig.get().COSTS.BACK.get());
    }

    private void addCommand(
        String command,
        Object value
    ) {

        // Generate a hashmap for each payment
        HashMap<String, Payment> payments = new HashMap<>();
        String valueStr = String.valueOf(value);
        if (valueStr.isEmpty()) {
            teleportCosts.put(command, payments);
            return;
        }
        String[] rawPayments = valueStr.split(";");
        for (String rawPayment : rawPayments) {
            try {
                Payment payment = parsePayment(rawPayment);
                if (payment == null) continue;
                addPayment(payment.getId(), payment, payments);
            } catch (Exception | NoClassDefFoundError e) {
                CoreClass.getInstance().getLogger().warning("Failed to parse payment " + rawPayment + " for command " + command + "! Error message: " + e.getMessage());
            }
        }
        teleportCosts.put(command, payments);
    }

    private Payment parsePayment(String rawPayment) {
        if (rawPayment.length() - 3 <= 0) {
            Matcher matcher = Pattern.compile("^(.+:)?([0-9]+(\\.[0-9]+)?)").matcher(rawPayment);
            if (matcher.matches()) {
                String plugin = matcher.group(1);
                double payment = Double.parseDouble(matcher.group(2));
                return new VaultPayment(payment, plugin == null ? null : plugin.substring(0, plugin.length() - 1));
            }
            return null;
        }
        String points = rawPayment.substring(0, rawPayment.length() - 3);
        if (rawPayment.endsWith("LVL")) {
            return new LevelsPayment(Integer.parseInt(points));
        } else if (rawPayment.endsWith("EXP")) {
            return new PointsPayment(Integer.parseInt(points));
        } else {
            Matcher matcher = Pattern.compile("^(.+:)?([0-9]+(\\.[0-9]+)?)").matcher(rawPayment);
            if (matcher.matches()) {
                String plugin = matcher.group(1);
                double payment = Double.parseDouble(matcher.group(2));

                // If it's not a plugin and actually an item, lmao
                if (plugin != null && Material.getMaterial(plugin) != null) {
                    return ItemsPayment.getFromString(rawPayment);
                }

                // Otherwise, resort to that
                return new VaultPayment(
                    payment,
                    plugin == null ? null : plugin.substring(0, plugin.length() - 1)
                );
            } else {
                return ItemsPayment.getFromString(rawPayment);
            }
        }
    }

    private void addPayment(
        String type,
        Payment payment,
        HashMap<String, Payment> currentPayMethods
    ) {
        if (type.equalsIgnoreCase("levels")) {
            if (currentPayMethods.containsKey("exp")) {
                PointsPayment existingPayment = (PointsPayment) currentPayMethods.get("exp");
                existingPayment.addLevels((LevelsPayment) payment);
            } else {
                PointsPayment newPayment = new PointsPayment(0);
                newPayment.addLevels((LevelsPayment) payment);
                currentPayMethods.put("exp", newPayment);
            }
        } else if (type.equalsIgnoreCase("item")) {
            if (payment == null) return;
            ItemsPayment itemsPayment = (ItemsPayment) payment;
            if (currentPayMethods.containsKey("item_" + itemsPayment.getMaterial().name())) {
                ItemsPayment otherPayment = (ItemsPayment) currentPayMethods.get("item_" + itemsPayment.getMaterial().name());
                otherPayment.setPaymentAmount(otherPayment.getPaymentAmount() + itemsPayment.getPaymentAmount());
                currentPayMethods.put("item_" + itemsPayment.getMaterial().name(), otherPayment);
            } else {
                currentPayMethods.put("item_" + itemsPayment.getMaterial().name(), itemsPayment);
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

    public static PaymentManager getInstance() {
        return instance;
    }

    // Method used to check if a player can pay for using a command
    public boolean canPay(
        String command,
        Player player,
        World toWorld
    ) {
        if (player.hasPermission("at.admin.bypass.payment")) return true;
        for (Payment payment : getPayments(command, player, toWorld).values()) {
            if (!payment.canPay(player)) {
                return false;
            }
        }
        return true;
    }

    // Method used to manage payments
    public void withdraw(
            String command,
            Player player,
            World toWorld
    ) {
        if (!player.hasPermission("at.admin.bypass.payment")) {
            for (Payment payment : getPayments(command, player, toWorld).values()) {
                payment.withdraw(player);
            }
        }
    }

    private HashMap<String, Payment> getPayments(
            String command,
            Player player,
            World toWorld
    ) {
        final var customCosts = MainConfig.get().CUSTOM_COSTS.get();
        var payments = new HashMap<String, Payment>();
        for (String key : customCosts.getKeys(false)) {
            String worldName = toWorld.getName().toLowerCase(Locale.ROOT);
            if (!player.hasPermission("at.member.cost." + key)
                && !player.hasPermission("at.member.cost." + command + "." + key)
                && !player.hasPermission("at.member.cost." + worldName + "." + key)
                && !player.hasPermission("at.member.cost." + command + "." + worldName + "." + key)) continue;
            String rawPayment = customCosts.getString(key);
            if (rawPayment == null) continue;
            Payment payment = parsePayment(rawPayment);
            if (payment == null) continue;
            payments.put(payment.getId(), payment);
        }
        if (payments.isEmpty()) payments = teleportCosts.get(command);
        return payments;
    }
}
