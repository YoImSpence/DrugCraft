package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.businesses.*;
import com.spence.drugcraft.cartel.*;
import com.spence.drugcraft.casino.*;
import com.spence.drugcraft.commands.*;
import com.spence.drugcraft.data.*;
import com.spence.drugcraft.dealer.DealerNPC;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.games.*;
import com.spence.drugcraft.gui.*;
import com.spence.drugcraft.handlers.*;
import com.spence.drugcraft.listeners.*;
import com.spence.drugcraft.listeners.CropListener;
import com.spence.drugcraft.police.*;
import com.spence.drugcraft.steeds.SteedManager;
import com.spence.drugcraft.town.TownCitizenManager;
import com.spence.drugcraft.utils.*;
import com.spence.drugcraft.steeds.*;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DrugCraft extends JavaPlugin {
    private static DrugCraft instance;
    private Economy economy;
    private PermissionManager permissionManager;
    private DataManager dataManager;
    private ConfigManager configManager;
    private MessageUtils messageUtils;
    private EconomyManager economyManager;
    private DrugManager drugManager;
    private AddictionManager addictionManager;
    private CartelManager cartelManager;
    private BusinessManager businessManager;
    private CasinoManager casinoManager;
    private NonCasinoGameManager nonCasinoGameManager;
    private PoliceManager policeManager;
    private PoliceLockupManager policeLockupManager;
    private DealerNPC dealerNPC;
    private TownCitizenManager townCitizenManager;
    private AdminGUI adminGUI;
    private AdminGUIHandler adminGUIHandler;
    private BusinessGUI businessGUI;
    private BusinessGUIHandler businessGUIHandler;
    private CartelGUI cartelGUI;
    private CartelGUIHandler cartelGUIHandler;
    private CasinoGUI casinoGUI;
    private CasinoGUIHandler casinoGUIHandler;
    private GamesGUI gamesGUI;
    private GamesGUIHandler gamesGUIHandler;
    private PlayerGUI playerGUI;
    private PlayerGUIHandler playerGUIHandler;
    private SteedGUI steedGUI;
    private SteedGUIHandler steedGUIHandler;
    private ChessGUI chessGUI;
    private CheckersGUI checkersGUI;
    private Connect4GUI connect4GUI;
    private RPSGUI rpsGUI;
    private Map<UUID, ActiveGUI> activeMenus = new HashMap<>();
    private final Logger logger = Logger.getLogger(DrugCraft.class.getName());

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfigs();
        registerTraits(); // Added: Register Citizens traits
        registerWorldGuardFlags(); // Added: Register WorldGuard flags
        initializeManagers();
        initializeGUIs();
        initializeListeners();
        registerCommands();
        MessageUtils.init(this);
        dealerNPC.spawnNPCs();
        townCitizenManager.spawnNPCs();
        new PoliceNPC(this).spawnNPCs();
        logger.info("DrugCraft enabled successfully.");
    }

    @Override
    public void onDisable() {
        activeMenus.clear();
        logger.info("DrugCraft disabled.");
    }

    public static DrugCraft getInstance() {
        return instance;
    }

    private void saveDefaultConfigs() {
        saveResource("data.yml", false);
        saveResource("cartels.yml", false);
        saveResource("drugs.yml", false);
        saveResource("unlocks.yml", false);
        saveResource("police.yml", false);
        saveResource("npcs.yml", false);
        saveResource("messages.yml", false);
    }

    private void registerTraits() {
        try {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DealerNPC.DealerTrait.class).withName("dealer"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TownCitizenManager.TownCitizenTrait.class).withName("town_citizen"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PoliceNPC.PoliceTrait.class).withName("police"));
            logger.info("Registered Citizens traits: dealer, town_citizen, police");
        } catch (Exception e) {
            logger.warning("Failed to register Citizens traits: " + e.getMessage());
        }
    }

    private void registerWorldGuardFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag npcTraverse = new StateFlag("npc-traverse", true);
            registry.register(npcTraverse);
            logger.info("Registered WorldGuard flag: npc-traverse");
        } catch (FlagConflictException e) {
            logger.warning("Failed to register npc-traverse flag: " + e.getMessage());
        }
    }

    private void initializeManagers() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        economy = economyProvider != null ? economyProvider.getProvider() : null;
        permissionManager = new PermissionManager();
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        economyManager = new EconomyManager(economy);
        drugManager = new DrugManager(this);
        addictionManager = new AddictionManager(this, dataManager, drugManager, policeManager);
        cartelManager = new CartelManager(this);
        businessManager = new BusinessManager(this, dataManager, economyManager, drugManager);
        casinoManager = new CasinoManager(this);
        nonCasinoGameManager = new NonCasinoGameManager(this);
        policeManager = new PoliceManager(this, drugManager, economy, permissionManager, cartelManager, new java.io.File(getDataFolder(), "police.yml"));
        policeLockupManager = new PoliceLockupManager(this);
        dealerNPC = new DealerNPC(this);
        townCitizenManager = new TownCitizenManager(this, drugManager);
    }

    private void initializeGUIs() {
        adminGUI = new AdminGUI(this, dataManager, drugManager, cartelManager, businessManager);
        adminGUIHandler = new AdminGUIHandler(this, adminGUI, dataManager, drugManager, cartelManager, businessManager);
        businessGUI = new BusinessGUI(this, businessManager);
        businessGUIHandler = new BusinessGUIHandler(this, businessGUI, businessManager, economyManager);
        cartelGUI = new CartelGUI(this, cartelManager);
        cartelGUIHandler = new CartelGUIHandler(this, cartelGUI, cartelManager, economyManager);
        casinoGUI = new CasinoGUI(this, casinoManager);
        casinoGUIHandler = new CasinoGUIHandler(this, casinoGUI, casinoManager);
        gamesGUI = new GamesGUI(this);
        gamesGUIHandler = new GamesGUIHandler(this, gamesGUI, nonCasinoGameManager);
        playerGUI = new PlayerGUI(this, dataManager, businessManager);
        playerGUIHandler = new PlayerGUIHandler(this, playerGUI);
        steedGUI = new SteedGUI(this);
        steedGUIHandler = new SteedGUIHandler(this, steedGUI, new SteedManager(this), economyManager);
        chessGUI = new ChessGUI(this);
        checkersGUI = new CheckersGUI(this);
        connect4GUI = new Connect4GUI(this);
        rpsGUI = new RPSGUI(this);
    }

    private void initializeListeners() {
        Bukkit.getPluginManager().registerEvents(new AddictionListener(this, drugManager, addictionManager), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CasinoGameListener(this, casinoManager), this);
        Bukkit.getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this, drugManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this, cartelManager), this);
        Bukkit.getPluginManager().registerEvents(new CartelStashListener(this, cartelManager), this);
        Bukkit.getPluginManager().registerEvents(new CartelStashBreakListener(this, cartelManager), this);
        Bukkit.getPluginManager().registerEvents(new CasinoRegionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NPCListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this, drugManager, addictionManager, new SteedManager(this)), this);
        Bukkit.getPluginManager().registerEvents(new PoliceListener(this, policeManager), this);
        Bukkit.getPluginManager().registerEvents(new SteedListener(this, new SteedManager(this)), this);
        Bukkit.getPluginManager().registerEvents(new CropListener(this, drugManager), this);
        Bukkit.getPluginManager().registerEvents(new DealerNPC(this).new DealerNPCListener(), this);
        Bukkit.getPluginManager().registerEvents(new TownCitizenManager(this, drugManager).new TownCitizenListener(), this);
        Bukkit.getPluginManager().registerEvents(new PoliceTrait.PoliceTraitListener(this), this);
    }

    private void registerCommands() {
        getCommand("business").setExecutor(new BusinessCommand(businessGUIHandler));
        getCommand("business").setTabCompleter(new BusinessTabCompleter());
        getCommand("cartel").setExecutor(new CartelCommand(cartelGUIHandler));
        getCommand("cartel").setTabCompleter(new CartelTabCompleter());
        getCommand("casino").setExecutor(new CasinoCommand(casinoGUIHandler));
        getCommand("casino").setTabCompleter(new CasinoTabCompleter());
        getCommand("games").setExecutor(new GamesCommand(gamesGUIHandler));
        getCommand("games").setTabCompleter(new GamesTabCompleter());
        getCommand("heists").setExecutor(new HeistsCommand());
        getCommand("heists").setTabCompleter(new HeistsTabCompleter());
        getCommand("player").setExecutor(new PlayerCommand(playerGUIHandler));
        getCommand("player").setTabCompleter(new PlayerTabCompleter());
        getCommand("admin").setExecutor(new AdminCommand(adminGUIHandler));
        getCommand("admin").setTabCompleter(new AdminTabCompleter());
    }

    public EconomyManager getEconomyManager() { return economyManager; }
    public DataManager getDataManager() { return dataManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public CartelManager getCartelManager() { return cartelManager; }
    public BusinessManager getBusinessManager() { return businessManager; }
    public CasinoManager getCasinoManager() { return casinoManager; }
    public NonCasinoGameManager getNonCasinoGameManager() { return nonCasinoGameManager; }
    public GamesGUI getGamesGUI() { return gamesGUI; }
    public DrugManager getDrugManager() { return drugManager; }
    public DealerGUIHandler getDealerGUIHandler() { return new DealerGUIHandler(this, new DealerGUI(this, drugManager), drugManager, economyManager); }
    public Map<UUID, ActiveGUI> getActiveMenus() { return activeMenus; }
    public PoliceLockupManager getLockupManager() { return policeLockupManager; }
    public AdminGUI getAdminGUI() { return adminGUI; }
    public BusinessGUI getBusinessGUI() { return businessGUI; }
    public CartelGUI getCartelGUI() { return cartelGUI; }
    public CasinoGUI getCasinoGUI() { return casinoGUI; }
    public PlayerGUI getPlayerGUI() { return playerGUI; }
    public SteedGUI getSteedGUI() { return steedGUI; }
}