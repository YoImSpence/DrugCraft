package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.admin.AdminCommand;
import com.spence.drugcraft.admin.AdminGUI;
import com.spence.drugcraft.businesses.*;
import com.spence.drugcraft.cartel.*;
import com.spence.drugcraft.casino.*;
import com.spence.drugcraft.commands.*;
import com.spence.drugcraft.crops.*;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.*;
import com.spence.drugcraft.games.GameManager;
import com.spence.drugcraft.games.GamesCommand;
import com.spence.drugcraft.gui.*;
import com.spence.drugcraft.levels.LevelsCommand;
import com.spence.drugcraft.levels.PlayerLevelsGUI;
import com.spence.drugcraft.listeners.*;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.town.*;
import com.spence.drugcraft.utils.*;
import com.spence.drugcraft.vehicles.VehicleCommand;
import com.spence.drugcraft.vehicles.VehicleManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DrugCraft extends JavaPlugin {
    private final Map<UUID, ActiveGUI> activeMenus = new HashMap<>();
    private CasinoGUI casinoGUI;
    private CropManager cropManager;
    private CartelStash cartelStash;
    private GrowLight growLight;
    private GUIListener guiListener;
    private TownCitizenManager townCitizenManager;
    private AddictionManager addictionManager;
    private PoliceManager policeManager;
    private PlayerLevelsGUIHandler levelsGUIHandler;
    private BusinessMachineGUI businessMachineGUI;
    private BusinessMachineGUIHandler businessMachineGUIHandler;
    private DealRequestGUI dealRequestGUI;
    private DealRequestGUIListener dealRequestGUIListener;
    private PermissionManager permissionManager;
    private BusinessManager businessManager;
    private CartelManager cartelManager;
    private CasinoManager casinoManager;
    private DataManager dataManager;
    private DrugManager drugManager;
    private EconomyManager economyManager;
    private VehicleManager vehicleManager;
    private Economy economy;
    private GameManager gameManager;
    private AdminGUI adminGUI;
    private CartelGUI cartelGUI;
    private PlayerLevelsGUI levelsGUI;
    private AdminGUIHandler adminGUIHandler;
    private CartelGUIHandler cartelGUIHandler;

    @Override
    public void onEnable() {
        initializeManagers();
        registerCommands();
        registerListeners();
        getLogger().info("DrugCraft plugin enabled");
    }

    private void initializeManagers() {
        permissionManager = new PermissionManager(this);
        casinoManager = new CasinoManager(this, economyManager);
        dataManager = new DataManager(this);
        drugManager = new DrugManager(this);
        economyManager = new EconomyManager(this);
        vehicleManager = new VehicleManager(this, economyManager);
        businessManager = new BusinessManager(this, dataManager, economyManager, drugManager);
        cartelManager = new CartelManager(this);
        casinoGUI = new CasinoGUI(this, casinoManager);
        cropManager = new CropManager(this);
        cartelStash = new CartelStash(this);
        growLight = new GrowLight(this);
        guiListener = new GUIListener(this);
        townCitizenManager = new TownCitizenManager(this);
        addictionManager = new AddictionManager(this, dataManager, drugManager, policeManager);
        policeManager = new PoliceManager(this, drugManager, economy, permissionManager, cartelManager, new PoliceConfig(new File(getDataFolder(), "police.yml")));
        levelsGUIHandler = new PlayerLevelsGUIHandler(this, new PlayerLevelsGUI(this, dataManager));
        businessMachineGUI = new BusinessMachineGUI(this, businessManager);
        businessMachineGUIHandler = new BusinessMachineGUIHandler(this, businessMachineGUI, businessManager, economyManager);
        dealRequestGUI = new DealRequestGUI(this);
        dealRequestGUIListener = new DealRequestGUIListener(this, dealRequestGUI, guiListener, townCitizenManager);
        gameManager = new GameManager(this, economyManager);
        adminGUI = new AdminGUI(this, dataManager, drugManager);
        cartelGUI = new CartelGUI(this);
        levelsGUI = new PlayerLevelsGUI(this, dataManager);
        adminGUIHandler = new AdminGUIHandler(this, adminGUI, dataManager);
        cartelGUIHandler = new CartelGUIHandler(this, cartelGUI, cartelManager);
    }

    private void registerCommands() {
        registerCommand("admin", new AdminCommand(adminGUIHandler, dataManager, drugManager), new AdminTabCompleter());
        registerCommand("cartel", new CartelCommand(this, cartelManager), new CartelTabCompleter());
        registerCommand("business", new BusinessCommand(this, businessManager), new BusinessTabCompleter());
        registerCommand("levels", new LevelsCommand(this, dataManager), new LevelsTabCompleter());
        registerCommand("steed", new VehicleCommand(this, vehicleManager), new SteedTabCompleter());
        registerCommand("casino", new CasinoCommand(this, casinoManager), new CasinoTabCompleter());
        registerCommand("games", new GamesCommand(this, new GamesGUIHandler(this, gameManager)), new GamesTabCompleter());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CropListener(this, drugManager, dataManager, policeManager), this);
        getServer().getPluginManager().registerEvents(new AddictionListener(this, drugManager, addictionManager), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this, drugManager, cropManager, growLight, dataManager), this);
        getServer().getPluginManager().registerEvents(new CartelStashBreakListener(this, cartelManager), this);
        getServer().getPluginManager().registerEvents(new CartelStashListener(this, cartelManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economyManager, townCitizenManager), this);
        getServer().getPluginManager().registerEvents(guiListener, this);
    }

    private void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter((org.bukkit.command.TabCompleter) tabCompleter);
        }
    }

    public Map<UUID, ActiveGUI> getActiveMenus() {
        return activeMenus;
    }

    public CasinoGUI getCasinoGUI() { return casinoGUI; }
    public CropManager getCropManager() { return cropManager; }
    public CartelStash getCartelStash() { return cartelStash; }
    public GrowLight getGrowLight() { return growLight; }
    public GUIListener getGuiListener() { return guiListener; }
    public TownCitizenManager getTownCitizenManager() { return townCitizenManager; }
    public AddictionManager getAddictionManager() { return addictionManager; }
    public PoliceManager getPoliceManager() { return policeManager; }
    public PlayerLevelsGUIHandler getLevelsGUIHandler() { return levelsGUIHandler; }
    public BusinessMachineGUI getBusinessMachineGUI() { return businessMachineGUI; }
    public BusinessMachineGUIHandler getBusinessMachineGUIHandler() { return businessMachineGUIHandler; }
    public DealRequestGUI getDealRequestGUI() { return dealRequestGUI; }
    public DealRequestGUIListener getDealRequestGUIListener() { return dealRequestGUIListener; }
    public PermissionManager getPermissionManager() { return permissionManager; }
    public BusinessManager getBusinessManager() { return businessManager; }
    public CartelManager getCartelManager() { return cartelManager; }
    public CasinoManager getCasinoManager() { return casinoManager; }
    public DataManager getDataManager() { return dataManager; }
    public DrugManager getDrugManager() { return drugManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public VehicleManager getVehicleManager() { return vehicleManager; }
    public Economy getEconomy() { return economy; }
    public GameManager getGameManager() { return gameManager; }
    public AdminGUI getAdminGUI() { return adminGUI; }
    public CartelGUI getCartelGUI() { return cartelGUI; }
    public PlayerLevelsGUI getLevelsGUI() { return levelsGUI; }
    public AdminGUIHandler getAdminGUIHandler() { return adminGUIHandler; }
    public CartelGUIHandler getCartelGUIHandler() { return cartelGUIHandler; }

    public FileConfiguration getConfig(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}