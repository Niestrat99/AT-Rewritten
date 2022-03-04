package io.github.niestrat99.advancedteleport.payments.types;

import io.github.niestrat99.advancedteleport.CoreClass;
import io.github.niestrat99.advancedteleport.config.CustomMessages;
import io.github.niestrat99.advancedteleport.payments.Payment;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collection;

public class VaultPayment extends Payment {

    private double price;
    private Economy economy;

    public VaultPayment(double price, String economyName) {
        this.price = price;
        if (economyName == null) {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (provider == null) throw new IllegalStateException("There is no economy provider registered.");
            economy = provider.getProvider();
            return;
        }
        Collection<RegisteredServiceProvider<Economy>> economies = Bukkit.getServicesManager().getRegistrations(Economy.class);
        for (RegisteredServiceProvider<Economy> provider : economies) {
            CoreClass.getInstance().getLogger().info("Checking " + provider.getPlugin().getName() + " against " + economyName);
            if (!provider.getPlugin().getName().equals(economyName)) continue;
            economy = provider.getProvider();
            break;
        }

        if (economy == null) throw new IllegalStateException("There is no economy provider for " + economyName + " registered.");
    }

    @Override
    public double getPaymentAmount() {
        return price;
    }

    @Override
    public void setPaymentAmount(double amount) {
        price = amount;
    }

    @Override
    public boolean canPay(Player player) {
        boolean result = super.canPay(player);
        if (!result) {
            CustomMessages.sendMessage(player, "Error.notEnoughMoney", "{amount}", economy.format(price));
        }
        return result;
    }

    @Override
    public double getPlayerAmount(Player player) {
        return economy.getBalance(player);
    }

    @Override
    public String getMessagePath() {
        return "Info.paymentVault";
    }

    @Override
    public void setPlayerAmount(Player player) {
        economy.withdrawPlayer(player, price);
        CustomMessages.sendMessage(player, "Info.paymentVault", "{amount}", economy.format(price), "{balance}", economy.format(getPlayerAmount(player)));
    }

}
