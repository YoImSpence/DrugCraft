package com.spence.drugcraft;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final DrugCraft plugin;
    private final Economy economy;

    public EconomyManager(DrugCraft plugin) {
        this.plugin = plugin;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        this.economy = rsp != null ? rsp.getProvider() : null;
        if (economy == null) {
            plugin.getLogger().severe("Vault Economy not found; economic features disabled.");
        }
    }

    public boolean hasBalance(Player player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public void withdrawMoney(Player player, double amount) {
        if (economy != null && economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
        }
    }

    public void depositMoney(Player player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }
}