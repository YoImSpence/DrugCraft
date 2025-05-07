package com.spence.drugcraft;

import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.data.ConfigManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.gui.AdminCommand;
import com.spence.drugcraft.listeners.*;
import com.spence.drugcraft.utils.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class DrugCraft extends JavaPlugin {
    private ConfigManager configManager;
    private DataManager dataManager;
    private CropManager cropManager;
    private DrugManager drugManager;
    private AddictionManager addictionManager;
    private CartelManager cartelManager;
    private EconomyManager economyManager;
    private PermissionManager permissionManager;
    private PoliceManager policeManager;
    private Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        drugManager = new DrugManager(this);
        economyManager = new EconomyManager(this);
        permissionManager = new PermissionManager(this, null); // Initialize with null CartelManager
        cartelManager = new CartelManager(this, dataManager, economyManager);
        permissionManager = new PermissionManager(this, cartelManager); // Re-initialize with CartelManager
        cropManager = new CropManager(this, drugManager, dataManager);
        addictionManager = new AddictionManager(this);
        policeManager = new PoliceManager(this, drugManager, economyManager, permissionManager, cartelManager);

        registerListeners();
        registerCommands();

        loadCrops();
        verifyCrops();
        logger.info("DrugCraft enabled successfully");
    }

    @Override
    public void onDisable() {
        cropManager.cleanupHolograms();
        policeManager.cleanupNPCs();
        dataManager.saveCrops();
        dataManager.saveCartels();
        addictionManager.saveAllPlayerData();
        logger.info("DrugCraft disabled successfully");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CropListener(this, drugManager, cropManager), this);
        getServer().getPluginManager().registerEvents(new AddictionListener(this, addictionManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economyManager.getEconomy()), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, drugManager, cartelManager), this);
        getServer().getPluginManager().registerEvents(new AdminGUIListener(this, drugManager), this);
        getServer().getPluginManager().registerEvents(new CartelGUIListener(this, cartelManager), this);
        getServer().getPluginManager().registerEvents(new PoliceListener(this, drugManager, economyManager, permissionManager, cartelManager), this);
    }

    private void registerCommands() {
        getCommand("drugadmin").setExecutor(new AdminCommand(this, drugManager));
    }

    private void loadCrops() {
        List<Crop> crops = dataManager.loadCrops();
        for (Crop crop : crops) {
            cropManager.addCrop(crop);
        }
    }

    private void verifyCrops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int verified = 0;
                for (Crop crop : cropManager.getCrops().values()) {
                    Block block = crop.getLocation().getBlock();
                    if (block.getType() != Material.WHEAT) {
                        logger.fine("Restoring wheat block for crop " + crop.getDrugId() + " at " + cropManager.getLocationKey(crop.getLocation()));
                        block.setType(Material.WHEAT);
                    }
                    verified++;
                }
                logger.info("Verified " + verified + " crops in world");
            }
        }.runTaskLater(this, 20L); // Run after world load
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public CropManager getCropManager() {
        return cropManager;
    }

    public DrugManager getDrugManager() {
        return drugManager;
    }

    public AddictionManager getAddictionManager() {
        return addictionManager;
    }

    public CartelManager getCartelManager() {
        return cartelManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public PoliceManager getPoliceManager() {
        return policeManager;
    }
}