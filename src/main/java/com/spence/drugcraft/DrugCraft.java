package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionListener;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.crops.CropListener;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.DrugCommand;
import com.spence.drugcraft.gui.DrugGUI;
import com.spence.drugcraft.gui.InventoryClickListener;
import com.spence.drugcraft.npcs.NPCListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DrugCraft extends JavaPlugin {
    private DrugManager drugManager;
    private CropManager cropManager;
    private AddictionManager addictionManager;
    private DrugGUI drugGUI;
    private DataManager dataManager;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("drugs.yml", false);
        saveResource("crops.yml", false);

        // Initialize managers
        drugManager = new DrugManager(this);
        dataManager = new DataManager(this);
        cropManager = new CropManager(this, drugManager, dataManager);
        addictionManager = new AddictionManager(this, dataManager);
        drugGUI = new DrugGUI(this, drugManager, economy);

        // Setup Vault economy
        if (!setupEconomy()) {
            getLogger().severe("Vault economy not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands and listeners
        getCommand("drug").setExecutor(new DrugCommand(this, drugGUI));
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this, drugGUI, drugManager, economy), this);
        getServer().getPluginManager().registerEvents(new CropListener(this, cropManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economy), this);
        getServer().getPluginManager().registerEvents(new AddictionListener(this, addictionManager), this);

        // Load persistent data
        dataManager.loadCrops();
        dataManager.loadPlayerData();

        getLogger().info("DrugCraft enabled successfully.");
    }

    @Override
    public void onDisable() {
        dataManager.saveCrops();
        dataManager.savePlayerData();
        getLogger().info("DrugCraft disabled successfully.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public DrugManager getDrugManager() {
        return drugManager;
    }

    public CropManager getCropManager() {
        return cropManager;
    }

    public AddictionManager getAddictionManager() {
        return addictionManager;
    }

    public DrugGUI getDrugGUI() {
        return drugGUI;
    }

    public Economy getEconomy() {
        return economy;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}