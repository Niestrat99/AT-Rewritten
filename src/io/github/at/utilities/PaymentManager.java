package io.github.at.utilities;

import io.github.at.config.Config;
import io.github.at.config.CustomMessages;
import io.github.at.main.CoreClass;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class PaymentManager {


    // Method used to check if a player can pay for using a command
    public static boolean canPay(String command, Player player) {
        if (!player.hasPermission("at.admin.bypass")) {
            if (Config.isUsingEXPPayment(command)){
                if (player.getLevel()<Config.getEXPTeleportPrice(command)){
                    player.sendMessage(CustomMessages.getString("Error.notEnoughEXP").replaceAll("\\{levels}", String.valueOf(Config.getEXPTeleportPrice(command))));
                    return false;
                }
            }
            if (CoreClass.getVault() != null && Config.isUsingVault(command)) {
                if (CoreClass.getVault().getBalance(player)<Config.getTeleportPrice(command)){
                    player.sendMessage(CustomMessages.getString("Error.notEnoughMoney").replaceAll("\\{amount}", String.valueOf(Config.getTeleportPrice(command))));
                    return false;
                }
            }
        }
        return true;
    }

    // Method used to manage payments
    public static void withdraw(String command, Player player) {
        if (!player.hasPermission("at.admin.bypass")) {
            if (Config.isUsingEXPPayment(command)) {
                if (player.getLevel() >= Config.getEXPTeleportPrice(command)){
                    int currentLevel = player.getLevel();
                    player.setLevel(currentLevel - Config.getEXPTeleportPrice(command));
                    player.sendMessage(CustomMessages.getString("Info.paymentEXP")
                            .replaceAll("\\{amount}", String.valueOf(Config.getEXPTeleportPrice(command)))
                            .replaceAll("\\{levels}", String.valueOf(player.getLevel())));
                }
            }
            if  (CoreClass.getVault() != null && Config.isUsingVault(command)) {
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
}
