package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropListener;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.AdminCommand;
import com.spence.drugcraft.gui.AdminGUI;
import com.spence.drugcraft.gui.CartelGUI;
import com.spence.drugcraft.listeners.AdminGUIListener;
import com.spence.drugcraft.listeners.AddictionListener;
import com.spence.drugcraft.listeners.CartelGUIListener;
import com.spence.drugcraft.listeners.NPCListener;
import com.spence.drugcraft.listeners.PlayerListener;
import com.spence.drugcraft.listeners.PoliceListener;
import com.spence.drugcraft.utils.CartelManager;
import com.spence.drugcraft.utils.ConfigManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.utils.PermissionManager;
import com.spence.drugcraft.utils.PoliceManager;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.logging.Logger;

public class DrugCraft extends JavaPlugin {
    private ConfigManager configManager;
    private DrugManager drugManager;
    private CropManager cropManager;
    private AddictionManager addictionManager;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private PermissionManager permissionManager;
    private CartelManager cartelManager;
    private PoliceManager policeManager;
    private Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        logger.info("Enabling DrugCraft v1.0.0");

        configManager = new ConfigManager(this);
        drugManager = new DrugManager(this);
        dataManager = new DataManager(this);
        cropManager = new CropManager(this, drugManager, dataManager);
        addictionManager = new AddictionManager(this, dataManager);
        economyManager = new EconomyManager(this);
        cartelManager = new CartelManager(this, dataManager, economyManager);
        permissionManager = new PermissionManager(this, cartelManager);
        policeManager = new PoliceManager(this, drugManager, economyManager, permissionManager, cartelManager);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new AddictionListener(this, addictionManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, drugManager, cartelManager), this);
        getServer().getPluginManager().registerEvents(new CropListener(this, cropManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economyManager.getEconomy()), this);
        getServer().getPluginManager().registerEvents(new AdminGUIListener(this, drugManager), this);
        getServer().getPluginManager().registerEvents(new PoliceListener(this, drugManager, economyManager, permissionManager, cartelManager), this);
        getServer().getPluginManager().registerEvents(new CartelGUIListener(this, cartelManager), this);

        // Register command executor
        this.getCommand("drugadmin").setExecutor(new AdminCommand(this, drugManager));
        this.getCommand("cartel").setExecutor(this);

        // Clean up orphaned crops and verify loaded crops
        cleanOrphanedCrops();
        verifyCrops();
    }

    @Override
    public void onDisable() {
        logger.info("Disabling DrugCraft v1.0.0");
        dataManager.saveCrops();
        dataManager.saveCartels();
        cropManager.cleanupHolograms();
        policeManager.cleanupNPCs();
        logger.info("Saved crops and cartels, cleaned up holograms and NPCs");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("drugadmin")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.color("{#FF5555}This command can only be used by players."));
                return true;
            }
            Player player = (Player) sender;
            if (!permissionManager.hasPermission(player, "drugcraft.admin")) {
                player.sendMessage(MessageUtils.color("{#FF5555}You do not have permission to use this command."));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("clearcrops")) {
                cropManager.clearAllCrops(player);
                return true;
            }
            new AdminGUI(this, drugManager).openGUI(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("cartel")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.color("{#FF5555}This command can only be used by players."));
                return true;
            }
            Player player = (Player) sender;
            if (!permissionManager.hasPermission(player, "drugcraft.cartel")) {
                player.sendMessage(MessageUtils.color("{#FF5555}You do not have permission to use this command."));
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(MessageUtils.color("{#FF5555}Usage: /cartel <create|invite|join|leave|info|manage>"));
                return true;
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.color("{#FF5555}Usage: /cartel create <name>"));
                    return true;
                }
                cartelManager.createCartel(player, args[1]);
                new CartelGUI(this, cartelManager).openGUI(player);
            } else if (args[0].equalsIgnoreCase("invite")) {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.color("{#FF5555}Usage: /cartel invite <player>"));
                    return true;
                }
                Player target = getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(MessageUtils.color("{#FF5555}Player not found!"));
                    return true;
                }
                cartelManager.invitePlayer(player, target);
            } else if (args[0].equalsIgnoreCase("join")) {
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.color("{#FF5555}Usage: /cartel join <cartelName>"));
                    return true;
                }
                cartelManager.joinCartel(player, args[1]);
            } else if (args[0].equalsIgnoreCase("leave")) {
                cartelManager.leaveCartel(player);
            } else if (args[0].equalsIgnoreCase("info")) {
                cartelManager.showCartelInfo(player);
            } else if (args[0].equalsIgnoreCase("manage")) {
                new CartelGUI(this, cartelManager).openGUI(player);
            } else {
                player.sendMessage(MessageUtils.color("{#FF5555}Usage: /cartel <create|invite|join|leave|info|manage>"));
            }
            return true;
        }
        return false;
    }

    private void cleanOrphanedCrops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int cleared = 0;
                for (World world : getServer().getWorlds()) {
                    for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                                    Block block = chunk.getBlock(x, y, z);
                                    if (block.getType() == Material.WHEAT) {
                                        Location loc = block.getLocation();
                                        if (cropManager.getCrop(loc) == null) {
                                            block.setType(Material.AIR);
                                            cleared++;
                                            logger.fine("Cleared orphaned wheat block at " + loc);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (cleared > 0) {
                    logger.info("Cleared " + cleared + " orphaned wheat blocks from loaded chunks");
                }
            }
        }.runTaskLater(this, 20L);
    }

    private void verifyCrops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int verified = 0;
                for (Crop crop : cropManager.getCrops().values()) {
                    Block block = crop.getLocation().getBlock();
                    if (block.getType() != Material.WHEAT) {
                        logger.warning("Crop at " + cropManager.getLocationKey(crop.getLocation()) + " is not wheat, restoring");
                        block.setType(Material.WHEAT);
                        Ageable ageable = (Ageable) block.getBlockData();
                        ageable.setAge(crop.getAge());
                        block.setBlockData(ageable);
                        block.getState().update(true, true);
                    }
                    verified++;
                }
                logger.info("Verified " + verified + " crops in world");
            }
        }.runTaskLater(this, 40L);
    }

    public ConfigManager getConfigManager() {
        return configManager;
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

    public DataManager getDataManager() {
        return dataManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public CartelManager getCartelManager() {
        return cartelManager;
    }

    public PoliceManager getPoliceManager() {
        return policeManager;
    }
}