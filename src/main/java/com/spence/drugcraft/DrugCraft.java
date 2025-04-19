package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.economy.EconomyManager;
import com.spence.drugcraft.npc.DealerListener;
import com.spence.drugcraft.npc.DealerManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DrugCraft extends JavaPlugin {
    private DrugManager drugManager;
    private EconomyManager economyManager;
    private AddictionManager addictionManager;
    private InputListener inputListener;
    private DrugGUI drugGUI;
    private DealerManager dealerManager;
    private DealerListener dealerListener;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize Managers
        drugManager = new DrugManager(this);
        Economy economy = getEconomy();
        economyManager = new EconomyManager(this, economy);
        addictionManager = new AddictionManager(this);
        inputListener = new InputListener(this);
        drugGUI = new DrugGUI(this);
        dealerManager = new DealerManager(this);
        getServer().getPluginManager().registerEvents(new DealerListener(this, dealerManager), this);


        // Register Commands
        getCommand("drug").setExecutor(new DrugCommand(this));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(drugGUI, this);
        getServer().getPluginManager().registerEvents(inputListener, this);

        // Start Withdrawal Task
        new WithdrawalTask(addictionManager).runTaskTimer(this, 0L, 1200L); // Every 60 seconds

        getLogger().info("DrugCraft enabled!");
    }

    @Override
    public void onDisable() {
        // Clear listeners to prevent accumulation on reload
        org.bukkit.event.HandlerList.unregisterAll(this);
        getLogger().info("DrugCraft disabled!");
    }

    public Economy getEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found! Economy features will be disabled.");
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No economy provider found!");
            return null;
        }
        return rsp.getProvider();
    }

    public DrugManager getDrugManager() {
        return drugManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public AddictionManager getAddictionManager() {
        return addictionManager;
    }

    public InputListener getInputListener() {
        return inputListener;
    }

    public DrugGUI getDrugGUI() {
        return drugGUI;
    }

    public Drug[] getDrugs() {
        return new Drug[0];
    }
}