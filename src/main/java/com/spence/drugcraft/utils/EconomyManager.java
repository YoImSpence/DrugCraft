package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyManager {
    private final DrugCraft plugin;
    private final Economy economy;

    public EconomyManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
    }

    public boolean isEconomyAvailable() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean withdrawPlayer(Player player, double amount) {
        if (!isEconomyAvailable()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean depositPlayer(Player player, double amount) {
        if (!isEconomyAvailable()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
}