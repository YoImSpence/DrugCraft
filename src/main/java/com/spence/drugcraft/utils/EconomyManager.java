package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public class EconomyManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private Economy economy;

    public EconomyManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Vault not found! Economy features will be disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            logger.severe("No economy provider found! Ensure an economy plugin (e.g., EssentialsX) is installed.");
            return;
        }
        economy = rsp.getProvider();
        if (economy != null) {
            logger.info("Vault economy hooked successfully: " + economy.getName());
        } else {
            logger.severe("Failed to hook Vault economy provider!");
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isEconomyAvailable() {
        return economy != null;
    }
}