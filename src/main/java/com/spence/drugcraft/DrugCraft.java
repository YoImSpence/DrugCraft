package com.spence.drugcraft;

import com.spence.drugcraft.listeners.AddictionListener;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.crops.CropListener;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.DrugCommand;
import com.spence.drugcraft.gui.DrugGUI;
import com.spence.drugcraft.listeners.InventoryClickListener;
import com.spence.drugcraft.listeners.PlayerListener;
import com.spence.drugcraft.listeners.NPCListener;
import com.spence.drugcraft.utils.ConfigManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.utils.PermissionManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.plugin.java.JavaPlugin;

public class DrugCraft extends JavaPlugin {
    private DrugManager drugManager;
    private CropManager cropManager;
    private AddictionManager addictionManager;
    private DrugGUI drugGUI;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private ConfigManager configManager;
    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.saveDefaultConfig();
        configManager.saveResource("drugs.yml", false);
        configManager.saveResource("crops.yml", false);
        configManager.saveResource("addiction.yml", false);

        // Initialize managers
        drugManager = new DrugManager(this);
        dataManager = new DataManager(this);
        cropManager = new CropManager(this, drugManager, dataManager);
        addictionManager = new AddictionManager(this, dataManager);
        economyManager = new EconomyManager(this);
        permissionManager = new PermissionManager(this);

        if (!economyManager.setupEconomy()) {
            getLogger().severe("Vault economy not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        drugGUI = new DrugGUI(this, drugManager, economyManager.getEconomy());

        // Register commands and listeners
        getCommand("drug").setExecutor(new DrugCommand(drugGUI));
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this, drugGUI, drugManager, economyManager.getEconomy()), this);
        getServer().getPluginManager().registerEvents(new CropListener(this, cropManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economyManager.getEconomy()), this);
        getServer().getPluginManager().registerEvents(new AddictionListener(this, addictionManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, drugManager), this);

        // Load persistent data
        dataManager.loadCrops();
        dataManager.loadPlayerData();

        // Register PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderExpansion(this).register();
        }

        MessageUtils.sendConsoleMessage("&aDrugCraft enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (addictionManager != null) {
            getServer().getOnlinePlayers().forEach(player ->
                    dataManager.savePlayerData(player.getUniqueId(), addictionManager.getPlayerData(player.getUniqueId())));
        }
        if (dataManager != null) {
            dataManager.saveCrops();
        }
        MessageUtils.sendConsoleMessage("&cDrugCraft disabled successfully.");
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

    public DataManager getDataManager() {
        return dataManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}