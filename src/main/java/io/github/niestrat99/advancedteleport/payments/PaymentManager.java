package io.github.niestrat99.advancedteleport.payments;

import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.config.Config;
import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.NewConfig;
import io.github.niestrat99.advancedteleport.payments.types.LevelsPayment;
import io.github.niestrat99.advancedteleport.payments.types.PointsPayment;
import io.github.niestrat99.advancedteleport.payments.types.VaultPayment;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PaymentManager {

    private HashMap<String, List<Payment>> teleportCosts;

    private static PaymentManager instance;

    public PaymentManager() {
        instance = this;
        teleportCosts = new HashMap<>();
        addCommand("tpa", NewConfig.getInstance().COSTS.TPA.get());
        addCommand("tpahere", NewConfig.getInstance().COSTS.TPAHERE.get());
        addCommand("tpr", NewConfig.getInstance().COSTS.TPR.get());
        addCommand("spawn", NewConfig.getInstance().COSTS.SPAWN.get());
        addCommand("warp", NewConfig.getInstance().COSTS.WARP.get());
        addCommand("home", NewConfig.getInstance().COSTS.HOME.get());
        addCommand("back", NewConfig.getInstance().COSTS.BACK.get());
    }

    private void addCommand(String command, Object value) {
        List<Payment> payments = new ArrayList<>();
        String[] rawPayments = String.valueOf(value).split(";");
        for (String rawPayment : rawPayments) {
            try {
                String points = rawPayment.substring(0, rawPayment.length() - 3);
                if (rawPayment.endsWith("LVL")) {
                    payments.add(new LevelsPayment(Integer.parseInt(points)));
                } else if (rawPayment.endsWith("EXP")) {
                    payments.add(new PointsPayment(Integer.parseInt(points)));
                } else {
                    if (CoreClass.getVault() != null) {
                        payments.add(new VaultPayment(Double.parseDouble(rawPayment)));
                    }
                }
            } catch (Exception e) {
                CoreClass.getInstance().getLogger().warning("Failed to parse payment " + rawPayment + " for command " + command + "!");
            }
        }
        teleportCosts.put(command, payments);
    }

    // Method used to check if a player can pay for using a command
    public boolean canPay(String command, Player player) {
        if (player.hasPermission("at.admin.bypass.payment")) return true;
        for (Payment payment : teleportCosts.get(command)) {
            if (!payment.canPay(player)) {
                return false;
            }
        }
        return true;
    }

    // Method used to manage payments
    public static void withdraw(String command, Player player) {
        if (!player.hasPermission("at.admin.bypass")) {
            if (Config.isUsingEXPPayment(command)) {
                if (Config.getEXPTeleportPrice(command) == 0) return;
                if (player.getLevel() >= Config.getEXPTeleportPrice(command)){
                    int currentLevel = player.getLevel();
                    player.setLevel(currentLevel - Config.getEXPTeleportPrice(command));
                    player.sendMessage(CustomMessages.getString("Info.paymentEXP")
                            .replaceAll("\\{amount}", String.valueOf(Config.getEXPTeleportPrice(command)))
                            .replaceAll("\\{levels}", String.valueOf(player.getLevel())));
                }
            }
            if  (CoreClass.getVault() != null && Config.isUsingVault(command)) {
                if (Config.getTeleportPrice(command) == 0) return;
                if (CoreClass.getVault().getBalance(player) >= Config.getTeleportPrice(command)){
                    EconomyResponse payment = CoreClass.getVault().withdrawPlayer(player, Config.getTeleportPrice(command));
                    if (payment.transactionSuccess()){
                        player.sendMessage(CustomMessages.getString("Info.paymentVault")
                                .replaceAll("\\{amount}", String.valueOf(Config.getTeleportPrice(command)))
                                .replaceAll("\\{balance}", String.valueOf(CoreClass.getVault().getBalance(player))));
                    }
                }
            }
        }
    }

    public static PaymentManager getInstance() {
        return instance;
    }
}
