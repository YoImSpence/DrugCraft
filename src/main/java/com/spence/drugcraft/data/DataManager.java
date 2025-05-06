package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.PlayerAddictionData;
import com.spence.drugcraft.crops.Crop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DataManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private File cropsFile;
    private FileConfiguration cropsConfig;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        setupCropsFile();
    }

    private void setupCropsFile() {
        cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        if (!cropsFile.exists()) {
            plugin.saveResource("crops.yml", false);
        }
        cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
    }

    public void saveCrops() {
        try {
            cropsConfig.set("crops", null); // Clear existing crops
            ConfigurationSection cropsSection = cropsConfig.createSection("crops");
            for (Crop crop : plugin.getCropManager().getCrops().values()) {
                String key = plugin.getCropManager().getLocationKey(crop.getLocation());
                ConfigurationSection cropSection = cropsSection.createSection(key);
                cropSection.set("world", crop.getLocation().getWorld().getName());
                cropSection.set("x", crop.getLocation().getBlockX());
                cropSection.set("y", crop.getLocation().getBlockY());
                cropSection.set("z", crop.getLocation().getBlockZ());
                cropSection.set("drug_id", crop.getDrugId());
                cropSection.set("planting_time", crop.getPlantingTime());
                cropSection.set("hologram_id", crop.getHologramId());
                cropSection.set("age", crop.getAge());
            }
            cropsConfig.save(cropsFile);
            logger.info("Saved " + plugin.getCropManager().getCrops().size() + " crops to crops.yml");
        } catch (IOException e) {
            logger.severe("Failed to save crops: " + e.getMessage());
        }
    }

    public void loadCrops() {
        try {
            cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
            ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
            if (cropsSection == null) {
                logger.info("No crops found in crops.yml");
                return;
            }
            for (String key : cropsSection.getKeys(false)) {
                ConfigurationSection cropSection = cropsSection.getConfigurationSection(key);
                if (cropSection != null) {
                    String worldName = cropSection.getString("world");
                    World world = plugin.getServer().getWorld(worldName);
                    if (world == null) {
                        logger.warning("Invalid world for crop '" + key + "': " + worldName + ", removing entry");
                        cropsSection.set(key, null);
                        continue;
                    }
                    int x = cropSection.getInt("x");
                    int y = cropSection.getInt("y");
                    int z = cropSection.getInt("z");
                    String drugId = cropSection.getString("drug_id");
                    long plantingTime = cropSection.getLong("planting_time");
                    String hologramId = cropSection.getString("hologram_id");
                    int age = cropSection.getInt("age", 0);
                    Location location = new Location(world, x, y, z);
                    location.setPitch(0);
                    location.setYaw(0);
                    Block block = location.getBlock();
                    // Validate farmland below
                    Block blockBelow = block.getRelative(0, -1, 0);
                    if (blockBelow.getType() != Material.FARMLAND) {
                        logger.warning("Block below crop at " + key + " is not farmland, removing entry");
                        cropsSection.set(key, null);
                        block.setType(Material.AIR); // Clear orphaned block
                        continue;
                    }
                    // Clear 5x5 area around crop (Y+1 and Y+2) to prevent duplicates
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            if (dx != 0 || dz != 0) { // Skip target crop position
                                Block relative = block.getRelative(dx, 1, dz);
                                if (relative.getType() == Material.WHEAT) {
                                    logger.fine("Clearing duplicate wheat block at " + relative.getLocation());
                                    relative.setType(Material.AIR);
                                }
                                relative = block.getRelative(dx, 0, dz);
                                if (relative.getType() == Material.WHEAT && plugin.getCropManager().getCrop(relative.getLocation()) == null) {
                                    logger.fine("Clearing adjacent wheat block at " + relative.getLocation());
                                    relative.setType(Material.AIR);
                                }
                            }
                        }
                    }
                    // Initialize wheat block
                    Crop crop = new Crop(location, drugId, plantingTime);
                    crop.setHologramId(hologramId);
                    crop.setAge(age);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            block.setType(Material.AIR); // Clear first to ensure clean state
                            block.setType(Material.WHEAT);
                            try {
                                block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                                Ageable ageable = (Ageable) block.getBlockData();
                                ageable.setAge(age);
                                block.setBlockData(ageable);
                                block.getState().update(true, true); // Force client update
                                logger.info("Restored crop block at " + key + " to wheat with age " + age);
                            } catch (ClassCastException e) {
                                logger.severe("Failed to set age for crop at " + key + ": " + e.getMessage());
                                block.setType(Material.AIR);
                                block.setType(Material.WHEAT);
                                block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                                block.getState().update(true, true);
                            }
                            plugin.getCropManager().addCrop(crop);
                        }
                    }.runTaskLater(plugin, 1L); // Delay to ensure block sync
                    logger.info("Loaded crop: " + key + " (drug: " + drugId + ", age: " + age + ")");
                }
            }
            try {
                cropsConfig.save(cropsFile); // Save after removing invalid entries
            } catch (IOException e) {
                logger.severe("Failed to save crops.yml after cleanup: " + e.getMessage());
            }
            logger.info("Loaded " + plugin.getCropManager().getCrops().size() + " crops from crops.yml");
        } catch (Exception e) {
            logger.severe("Failed to load crops: " + e.getMessage());
        }
    }

    public void saveCrop(Crop crop) {
        try {
            ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
            if (cropsSection == null) {
                cropsSection = cropsConfig.createSection("crops");
            }
            String key = plugin.getCropManager().getLocationKey(crop.getLocation());
            ConfigurationSection cropSection = cropsSection.createSection(key);
            cropSection.set("world", crop.getLocation().getWorld().getName());
            cropSection.set("x", crop.getLocation().getBlockX());
            cropSection.set("y", crop.getLocation().getBlockY());
            cropSection.set("z", crop.getLocation().getBlockZ());
            cropSection.set("drug_id", crop.getDrugId());
            cropSection.set("planting_time", crop.getPlantingTime());
            cropSection.set("hologram_id", crop.getHologramId());
            cropSection.set("age", crop.getAge());
            cropsConfig.save(cropsFile);
            logger.info("Saved crop: " + crop.getDrugId() + " at " + key);
        } catch (IOException e) {
            logger.severe("Failed to save crop: " + e.getMessage());
        }
    }

    public void removeCrop(Crop crop) {
        try {
            ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
            if (cropsSection != null) {
                String key = plugin.getCropManager().getLocationKey(crop.getLocation());
                cropsSection.set(key, null);
                cropsConfig.save(cropsFile);
                logger.info("Removed crop: " + crop.getDrugId() + " from " + key);
            }
        } catch (IOException e) {
            logger.severe("Failed to save crops after removal: " + e.getMessage());
        }
    }

    public void savePlayerData(UUID playerId, PlayerAddictionData data) {
        FileConfiguration addictionConfig = plugin.getConfigManager().getConfig();
        ConfigurationSection playerSection = addictionConfig.createSection("addiction." + playerId.toString());
        for (Map.Entry<String, Integer> entry : data.getUsesMap().entrySet()) {
            playerSection.set(entry.getKey(), entry.getValue());
        }
        plugin.getConfigManager().saveConfig();
    }

    public PlayerAddictionData loadPlayerData(UUID playerId) {
        FileConfiguration addictionConfig = plugin.getConfigManager().getConfig();
        ConfigurationSection playerSection = addictionConfig.getConfigurationSection("addiction." + playerId.toString());
        PlayerAddictionData data = new PlayerAddictionData();
        if (playerSection != null) {
            for (String drugId : playerSection.getKeys(false)) {
                data.setUses(drugId, playerSection.getInt(drugId));
            }
        }
        return data;
    }
}