package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.admin.AdminGUI;
import com.spence.drugcraft.listeners.AdminGUIListener;
import com.spence.drugcraft.cartel.CartelCommand;
import com.spence.drugcraft.listeners.CartelGUIListener;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.listeners.CartelStashListener;
import com.spence.drugcraft.listeners.BlockPlaceListener;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.utils.ConfigManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.houses.HouseCommand;
import com.spence.drugcraft.houses.HouseManager;
import com.spence.drugcraft.listeners.AddictionListener;
import com.spence.drugcraft.listeners.BlockBreakListener;
import com.spence.drugcraft.listeners.NPCListener;
import com.spence.drugcraft.listeners.PlayerListener;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.PermissionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public class DrugCraft extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;
    private DrugManager drugManager;
    private EconomyManager economyManager;
    private PermissionManager permissionManager;
    private AddictionManager addictionManager;
    private CartelManager cartelManager;
    private CropManager cropManager;
    private HouseManager houseManager;
    private GrowLight growLight;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        drugManager = new DrugManager(this);
        economyManager = new EconomyManager(this);
        permissionManager = new PermissionManager(this);
        dataManager = new DataManager(this);
        addictionManager = new AddictionManager(this, dataManager);
        cartelManager = new CartelManager(this, dataManager, economyManager);
        cropManager = new CropManager(this, drugManager, dataManager, growLight);
        houseManager = new HouseManager(this, economyManager);
        growLight = new GrowLight(cropManager, drugManager);

        // Now that all managers are initialized, load crops
        dataManager.loadCrops();

        Economy economy = economyManager.getEconomy();
        getServer().getPluginManager().registerEvents(new AddictionListener(this, dataManager, drugManager, addictionManager), this);
        getServer().getPluginManager().registerEvents(new AdminGUIListener(this, drugManager, growLight), this);
        getServer().getPluginManager().registerEvents(new CartelGUIListener(this, cartelManager), this);
        getServer().getPluginManager().registerEvents(new CartelStashListener(this, cartelManager, dataManager), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this, cropManager, drugManager, growLight), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(growLight), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economy), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, cropManager, drugManager, growLight), this);

        getCommand("cartel").setExecutor(new CartelCommand(this, cartelManager));
        getCommand("house").setExecutor(new HouseCommand(this, houseManager));

        getLogger().info("DrugCraft enabled successfully");
    }

    @Override
    public void onDisable() {
        if (cropManager != null) {
            cropManager.cleanupHolograms();
        }
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("DrugCraft disabled successfully");
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

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public AddictionManager getAddictionManager() {
        return addictionManager;
    }

    public CartelManager getCartelManager() {
        return cartelManager;
    }

    public CropManager getCropManager() {
        return cropManager;
    }

    public HouseManager getHouseManager() {
        return houseManager;
    }
}