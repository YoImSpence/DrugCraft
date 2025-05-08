package com.spence.drugcraft;

import com.spence.drugcraft.admin.AdminCommand;
import com.spence.drugcraft.listeners.BlockBreakListener;
import com.spence.drugcraft.listeners.BlockPlaceListener;
import com.spence.drugcraft.crops.CropListener;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.listeners.PlayerInteractListener;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.admin.AdminGUI;
import com.spence.drugcraft.listeners.AdminGUIListener;
import com.spence.drugcraft.listeners.AddictionListener;
import com.spence.drugcraft.listeners.NPCListener;
import com.spence.drugcraft.listeners.PlayerListener;
import com.spence.drugcraft.data.ConfigManager;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.PermissionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public class DrugCraft extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;
    private DrugManager drugManager;
    private CropManager cropManager;
    private PermissionManager permissionManager;
    private EconomyManager economyManager;
    private CartelManager cartelManager;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        drugManager = new DrugManager(this);
        cropManager = new CropManager(this, drugManager, dataManager);
        permissionManager = new PermissionManager();
        economyManager = new EconomyManager(this);
        cartelManager = new CartelManager(this);

        // Initialize GUI
        AdminGUI adminGUI = new AdminGUI(this, drugManager);

        // Register commands
        getCommand("drugadmin").setExecutor(new AdminCommand(this, cropManager, adminGUI));

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this, cropManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new CropListener(this, cropManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new AddictionListener(this, dataManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, dataManager, cropManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new AdminGUIListener(this, drugManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, getEconomy()), this);

        getLogger().info("DrugCraft has been enabled!");
    }

    @Override
    public void onDisable() {
        cropManager.cleanupHolograms();
        dataManager.saveCrops();
        dataManager.savePlayerData();
        getLogger().info("DrugCraft has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public DrugManager getDrugManager() {
        return drugManager;
    }

    public CropManager getCropManager() {
        return cropManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public CartelManager getCartelManager() {
        return cartelManager;
    }

    public Economy getEconomy() {
        return economyManager.getEconomy();
    }
}