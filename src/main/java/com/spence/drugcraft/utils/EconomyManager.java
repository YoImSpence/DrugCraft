package com.spence.drugcraft.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

public class EconomyManager {
    private final Economy economy;

    public EconomyManager(Economy economy) {
        this.economy = economy;
    }

    public boolean isEconomyAvailable() {
        return economy != null;
    }

    public boolean withdrawPlayer(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable() || !economy.has(player, amount)) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean depositPlayer(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public double getBalance(OfflinePlayer player) {
        if (!isEconomyAvailable()) return 0.0;
        return economy.getBalance(player);
    }
}