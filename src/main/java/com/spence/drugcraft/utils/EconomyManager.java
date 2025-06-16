package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;

public class EconomyManager {
    private final DrugCraft plugin;
    private Economy economy;

    public EconomyManager(DrugCraft plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        Plugin vaultPlugin = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null) {
            plugin.getLogger().severe("Vault plugin not found! Economy features will be disabled. Ensure Vault is installed and enabled.");
            return;
        }
        int retryCount = 0;
        while (retryCount < 3) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                plugin.getLogger().warning("No economy provider found on attempt " + (retryCount + 1) + ". Retrying...");
                retryCount++;
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                continue;
            }
            economy = rsp.getProvider();
            if (economy == null || !economy.isEnabled()) {
                plugin.getLogger().severe("Economy provider '" + (economy != null ? economy.getName() : "null") + "' is not enabled or functional! Economy features will be disabled.");
                economy = null;
                retryCount++;
                continue;
            }
            try {
                economy.hasAccount(plugin.getServer().getOfflinePlayer("test"));
                plugin.getLogger().info("Economy provider found and functional: " + economy.getName());
                return;
            } catch (Exception e) {
                plugin.getLogger().severe("Economy provider '" + economy.getName() + "' failed validation: " + e.getMessage() + ". Retrying...");
                economy = null;
                retryCount++;
            }
        }
        plugin.getLogger().severe("Failed to find a functional economy provider after 3 attempts. Economy features disabled.");
    }

    public Economy getEconomy() {
        if (economy == null) {
            setupEconomy();
        }
        return economy;
    }

    public boolean isEconomyAvailable() {
        if (economy == null) {
            setupEconomy();
        }
        return economy != null;
    }
}