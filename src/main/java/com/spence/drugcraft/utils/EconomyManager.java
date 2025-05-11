package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final DrugCraft plugin;
    private Economy economy;

    public EconomyManager(DrugCraft plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault plugin not found! Economy features will be disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().severe("No economy provider found! Economy features will be disabled.");
            return;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Economy provider found: " + economy.getName());
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isEconomyAvailable() {
        return economy != null;
    }
}