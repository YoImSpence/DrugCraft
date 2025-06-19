package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.commands.AdminCommand;
import com.spence.drugcraft.gui.*;
import com.spence.drugcraft.handlers.*;
import com.spence.drugcraft.businesses.*;
import com.spence.drugcraft.cartel.*;
import com.spence.drugcraft.casino.*;
import com.spence.drugcraft.commands.*;
import com.spence.drugcraft.crops.*;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.dealer.DealerNPC;
import com.spence.drugcraft.drugs.*;
import com.spence.drugcraft.commands.GamesCommand;
import com.spence.drugcraft.games.NonCasinoGameManager;
import com.spence.drugcraft.heists.HeistManager;
import com.spence.drugcraft.commands.HeistsCommand;
import com.spence.drugcraft.listeners.*;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.police.PoliceNPC;
import com.spence.drugcraft.town.*;
import com.spence.drugcraft.utils.*;
import com.spence.drugcraft.vehicles.SteedsCommand;
import com.spence.drugcraft.vehicles.VehicleManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

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
    private PlayerGUIHandler playerGUIHandler;
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
    private NonCasinoGameManager gameManager;
    private AdminGUI adminGUI;
    private CartelGUI cartelGUI;
    private PlayerGUI playerGUI;
    private AdminGUIHandler adminGUIHandler;
    private CartelGUIHandler cartelGUIHandler;
    private CasinoGUIHandler casinoGUIHandler;
    private DealerGUI dealerGUI;
    private DealerGUIHandler dealerGUIHandler;
    private VehicleGUI vehicleGUI;
    private VehicleGUIHandler vehicleGUIHandler;
    private GamesGUIHandler gamesGUIHandler;
    private HeistManager heistManager;
    private HeistGUI heistGUI;
    private HeistGUIHandler heistGUIHandler;
    private ConfigManager configManager;
    private DealerNPC dealerNPC;
    private PoliceNPC policeNPC;
    private RegionVisualizer regionVisualizer;
    private BusinessGUI businessGUI;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        MessageUtils.init(this);
        initializeManagers();
        registerCommands();
        registerListeners();
        getLogger().info("DrugCraft plugin enabled");
    }

    private void initializeManagers() {
        permissionManager = new PermissionManager(this);
        economyManager = new EconomyManager(this);
        casinoManager = new CasinoManager(this, economyManager);
        dataManager = new DataManager(this);
        drugManager = new DrugManager(this);
        vehicleManager = new VehicleManager(this, economyManager);
        businessManager = new BusinessManager(this, dataManager, economyManager, drugManager);
        cartelManager = new CartelManager(this);
        heistManager = new HeistManager(this, economyManager);
        casinoGUI = new CasinoGUI(this, casinoManager);
        casinoGUIHandler = new CasinoGUIHandler(this, casinoGUI);
        cropManager = new CropManager(this);
        cartelStash = new CartelStash(this);
        growLight = new GrowLight(this);
        guiListener = new GUIListener(this);
        townCitizenManager = new TownCitizenManager(this, drugManager);
        addictionManager = new AddictionManager(this, dataManager, drugManager, policeManager);
        policeManager = new PoliceManager(this, drugManager, economy, permissionManager, new PoliceManager.PoliceConfig(new File(getDataFolder(), "police.yml")));
        playerGUIHandler = new PlayerGUIHandler(this, new PlayerGUI(this, dataManager, businessManager));
        businessMachineGUI = new BusinessMachineGUI(this, businessManager);
        businessMachineGUIHandler = new BusinessMachineGUIHandler(this, businessMachineGUI, businessManager, economyManager);
        dealRequestGUI = new DealRequestGUI(this);
        dealRequestGUIListener = new DealRequestGUIListener(this, dealRequestGUI, guiListener, townCitizenManager);
        gameManager = new NonCasinoGameManager(this);
        gamesGUIHandler = new GamesGUIHandler(this, gameManager);
        adminGUI = new AdminGUI(this, dataManager, drugManager, cartelManager, businessManager);
        cartelGUI = new CartelGUI(this, cartelManager);
        playerGUI = new PlayerGUI(this, dataManager, businessManager);
        adminGUIHandler = new AdminGUIHandler(this, adminGUI, dataManager, drugManager, cartelManager, businessManager);
        cartelGUIHandler = new CartelGUIHandler(this, cartelGUI);
        dealerGUI = new DealerGUI(this, drugManager);
        dealerGUIHandler = new DealerGUIHandler(this, dealerGUI, drugManager, economyManager);
        vehicleGUI = new VehicleGUI(this, vehicleManager, dataManager);
        vehicleGUIHandler = new VehicleGUIHandler(this, vehicleGUI);
        heistGUI = new HeistGUI(this);
        heistGUIHandler = new HeistGUIHandler(this, heistGUI, heistManager);
        dealerNPC = new DealerNPC(this);
        policeNPC = new PoliceNPC(this);
        regionVisualizer = new RegionVisualizer(this);
        businessGUI = new BusinessGUI(this, businessManager);
    }

    private void registerCommands() {
        registerCommand("admin", new AdminCommand(adminGUIHandler), new AdminTabCompleter());
        registerCommand("businesses", new BusinessCommand(this, businessManager), new BusinessTabCompleter());
        registerCommand("cartel", new CartelCommand(this, cartelManager), new CartelTabCompleter());
        registerCommand("casino", new CasinoCommand(this), new CasinoTabCompleter());
        registerCommand("games", new GamesCommand(this, gamesGUIHandler), new GamesTabCompleter());
        registerCommand("heists", new HeistsCommand(this, heistGUIHandler), new HeistsTabCompleter());
        registerCommand("player", new PlayerCommand(this, playerGUIHandler), new PlayerTabCompleter());
        registerCommand("steeds", new SteedsCommand(this, vehicleManager), new SteedTabCompleter());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CropListener(this, drugManager, dataManager, policeManager), this);
        getServer().getPluginManager().registerEvents(new AddictionListener(this, drugManager, addictionManager), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this, drugManager, cropManager, growLight, dataManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this, drugManager, cropManager, growLight, dataManager, policeManager), this);
        getServer().getPluginManager().registerEvents(new CartelStashBreakListener(this, cartelManager), this);
        getServer().getPluginManager().registerEvents(new CartelStashListener(this, cartelManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economyManager, townCitizenManager), this);
        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(new CasinoRegionListener(this), this);
        getServer().getPluginManager().registerEvents(new SteedListener(this, vehicleManager), this);
        getServer().getPluginManager().registerEvents(new HeistListener(this, heistManager), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this, vehicleManager), this);
    }

    private void registerCommand(String name, CommandExecutor executor, org.bukkit.command.TabCompleter tabCompleter) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(tabCompleter);
        }
    }

    public Map<UUID, ActiveGUI> getActiveMenus() { return activeMenus; }
    public CasinoGUI getCasinoGUI() { return casinoGUI; }
    public CropManager getCropManager() { return cropManager; }
    public CartelStash getCartelStash() { return cartelStash; }
    public GrowLight getGrowLight() { return growLight; }
    public GUIListener getGuiListener() { return guiListener; }
    public TownCitizenManager getTownCitizenManager() { return townCitizenManager; }
    public AddictionManager getAddictionManager() { return addictionManager; }
    public PoliceManager getPoliceManager() { return policeManager; }
    public PlayerGUIHandler getPlayerGUIHandler() { return playerGUIHandler; }
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
    public NonCasinoGameManager getGameManager() { return gameManager; }
    public AdminGUI getAdminGUI() { return adminGUI; }
    public CartelGUI getCartelGUI() { return cartelGUI; }
    public PlayerGUI getPlayerGUI() { return playerGUI; }
    public AdminGUIHandler getAdminGUIHandler() { return adminGUIHandler; }
    public CartelGUIHandler getCartelGUIHandler() { return cartelGUIHandler; }
    public CasinoGUIHandler getCasinoGUIHandler() { return casinoGUIHandler; }
    public DealerGUI getDealerGUI() { return dealerGUI; }
    public DealerGUIHandler getDealerGUIHandler() { return dealerGUIHandler; }
    public VehicleGUI getVehicleGUI() { return vehicleGUI; }
    public VehicleGUIHandler getVehicleGUIHandler() { return vehicleGUIHandler; }
    public GamesGUIHandler getGamesGUIHandler() { return gamesGUIHandler; }
    public HeistGUIHandler getHeistGUIHandler() { return heistGUIHandler; }
    public ConfigManager getConfigManager() { return configManager; }
    public DealerNPC getDealerNPC() { return dealerNPC; }
    public PoliceNPC getPoliceNPC() { return policeNPC; }
    public RegionVisualizer getRegionVisualizer() { return regionVisualizer; }
    public BusinessGUI getBusinessGUI() { return businessGUI; }

    public FileConfiguration getConfig(String fileName) {
        return configManager.getConfig(fileName);
    }

    public void saveConfig() {
        configManager.saveConfig("data.yml");
    }
}