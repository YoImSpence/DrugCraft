package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.AdminCommand;
import com.spence.drugcraft.listeners.AddictionListener;
import com.spence.drugcraft.listeners.PlayerListener;
import com.spence.drugcraft.listeners.NPCListener;
import com.spence.drugcraft.utils.ConfigManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.utils.PermissionManager;
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
        permissionManager = new PermissionManager(this);

        getServer().getPluginManager().registerEvents(new AddictionListener(this, addictionManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, addictionManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new com.spence.drugcraft.crops.CropListener(this, cropManager, drugManager), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this, drugManager, economyManager), this);
        getServer().getPluginManager().registerEvents(new com.spence.drugcraft.gui.AdminGUIListener(this, drugManager), this);

        this.getCommand("drugadmin").setExecutor(new AdminCommand(this, drugManager));

        // Clean up orphaned crops on enable
        cleanOrphanedCrops();
    }

    @Override
    public void onDisable() {
        logger.info("Disabling DrugCraft v1.0.0");
        dataManager.saveCrops();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("drugadmin")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.colorize("&cThis command can only be used by players."));
                return true;
            }
            Player player = (Player) sender;
            if (!permissionManager.hasPermission(player, "drugcraft.admin")) {
                player.sendMessage(MessageUtils.colorize("&cYou do not have permission to use this command."));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("clearcrops")) {
                clearAllCrops(player);
                return true;
            }
            new com.spence.drugcraft.gui.AdminGUI(this, drugManager).openGUI(player);
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
        }.runTaskLater(this, 20L); // Run after 1 second to ensure plugin is fully loaded
    }

    private void clearAllCrops(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int cleared = 0;
                for (Crop crop : cropManager.getCrops().values()) {
                    Block block = crop.getLocation().getBlock();
                    block.setType(Material.AIR);
                    cropManager.removeCrop(crop);
                    cleared++;
                }
                dataManager.saveCrops(); // Clear crops.yml
                player.sendMessage(MessageUtils.colorize("&aCleared " + cleared + " drug crops and their data."));
                logger.info("Player " + player.getName() + " cleared " + cleared + " drug crops");
            }
        }.runTask(this);
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
}