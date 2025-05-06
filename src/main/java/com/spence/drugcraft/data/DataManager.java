package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.PlayerAddictionData;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.utils.CartelManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DataManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private File cropsFile;
    private FileConfiguration cropsConfig;
    private File cartelsFile;
    private FileConfiguration cartelsConfig;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        setupCropsFile();
        setupCartelsFile();
    }

    private void setupCropsFile() {
        cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        if (!cropsFile.exists()) {
            try {
                cropsFile.getParentFile().mkdirs();
                plugin.saveResource("crops.yml", false);
                logger.info("Created new crops.yml file");
            } catch (Exception e) {
                logger.severe("Failed to create crops.yml: " + e.getMessage());
            }
        }
        cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
    }

    private void setupCartelsFile() {
        cartelsFile = new File(plugin.getDataFolder(), "cartels.yml");
        if (!cartelsFile.exists()) {
            try {
                cartelsFile.getParentFile().mkdirs();
                plugin.saveResource("cartels.yml", false);
                logger.info("Created new cartels.yml file");
            } catch (Exception e) {
                logger.severe("Failed to create cartels.yml: " + e.getMessage());
            }
        }
        cartelsConfig = YamlConfiguration.loadConfiguration(cartelsFile);
    }

    public void saveCrops() {
        try {
            cropsConfig.set("crops", null);
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
                cropSection.set("hologram_id", null);
                cropSection.set("age", crop.getAge());
                logger.fine("Saving crop: " + crop.getDrugId() + " at " + key);
            }
            int retries = 3;
            while (retries > 0) {
                try {
                    cropsConfig.save(cropsFile);
                    logger.info("Saved " + plugin.getCropManager().getCrops().size() + " crops to crops.yml");
                    break;
                } catch (IOException e) {
                    retries--;
                    if (retries == 0) {
                        logger.severe("Failed to save crops after retries: " + e.getMessage());
                    } else {
                        logger.warning("Retrying saveCrops due to: " + e.getMessage());
                        Thread.sleep(100);
                    }
                }
            }
        } catch (Exception e) {
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
                    int age = cropSection.getInt("age", 0);
                    Location location = new Location(world, x, y, z);
                    location.setPitch(0);
                    location.setYaw(0);
                    Block block = location.getBlock();
                    Block blockBelow = block.getRelative(0, -1, 0);
                    if (blockBelow.getType() != Material.FARMLAND) {
                        logger.warning("Block below crop at " + key + " is not farmland, removing entry");
                        cropsSection.set(key, null);
                        block.setType(Material.AIR);
                        continue;
                    }
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            if (dx != 0 || dz != 0) {
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
                    Crop crop = new Crop(location, drugId, plantingTime);
                    crop.setAge(age);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            block.setType(Material.AIR);
                            block.setType(Material.WHEAT);
                            try {
                                block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                                Ageable ageable = (Ageable) block.getBlockData();
                                ageable.setAge(age);
                                block.setBlockData(ageable);
                                block.getState().update(true, true);
                                logger.info("Restored crop block at " + key + " to wheat with age " + age);
                            } catch (ClassCastException e) {
                                logger.severe("Failed to set age for crop at " + key + ": " + e.getMessage());
                                block.setType(Material.AIR);
                                block.setType(Material.WHEAT);
                                block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                                block.getState().update(true, true);
                            }
                            plugin.getCropManager().addCrop(crop);
                            logger.info("Loaded crop: " + key + " (drug: " + drugId + ", age: " + age + ")");
                        }
                    }.runTaskLater(plugin, 1L);
                }
            }
            try {
                cropsConfig.save(cropsFile);
                logger.info("Updated crops.yml after cleanup");
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
            cropSection.set("hologram_id", null);
            cropSection.set("age", crop.getAge());
            int retries = 3;
            while (retries > 0) {
                try {
                    cropsConfig.save(cropsFile);
                    logger.info("Saved crop: " + crop.getDrugId() + " at " + key);
                    break;
                } catch (IOException e) {
                    retries--;
                    if (retries == 0) {
                        logger.severe("Failed to save crop after retries: " + e.getMessage());
                    } else {
                        logger.warning("Retrying saveCrop due to: " + e.getMessage());
                        Thread.sleep(100);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to save crop: " + e.getMessage());
        }
    }

    public void removeCrop(Crop crop) {
        try {
            ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
            if (cropsSection != null) {
                String key = plugin.getCropManager().getLocationKey(crop.getLocation());
                cropsSection.set(key, null);
                int retries = 3;
                while (retries > 0) {
                    try {
                        cropsConfig.save(cropsFile);
                        logger.info("Removed crop: " + crop.getDrugId() + " from " + key);
                        break;
                    } catch (IOException e) {
                        retries--;
                        if (retries == 0) {
                            logger.severe("Failed to save crops after removal: " + e.getMessage());
                        } else {
                            logger.warning("Retrying removeCrop due to: " + e.getMessage());
                            Thread.sleep(100);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to save crops after removal: " + e.getMessage());
        }
    }

    public void saveCartels() {
        try {
            cartelsConfig.set("cartels", null);
            ConfigurationSection cartelsSection = cartelsConfig.createSection("cartels");
            for (CartelManager.Cartel cartel : plugin.getCartelManager().getCartels().values()) {
                ConfigurationSection cartelSection = cartelsSection.createSection(cartel.getName());
                cartelSection.set("leader", cartel.getLeader().toString());
                List<String> memberUUIDs = cartel.getMembers().stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList());
                cartelSection.set("members", memberUUIDs);
                cartelSection.set("level", cartel.getLevel());
                cartelSection.set("stashed_money", cartel.getStashedMoney());
                cartelSection.set("permissions", cartel.getPermissions());
                cartelSection.set("upgrades", cartel.getUpgrades());
                logger.fine("Saving cartel: " + cartel.getName());
            }
            int retries = 3;
            while (retries > 0) {
                try {
                    cartelsConfig.save(cartelsFile);
                    logger.info("Saved " + plugin.getCartelManager().getCartels().size() + " cartels to cartels.yml");
                    break;
                } catch (IOException e) {
                    retries--;
                    if (retries == 0) {
                        logger.severe("Failed to save cartels after retries: " + e.getMessage());
                    } else {
                        logger.warning("Retrying saveCartels due to: " + e.getMessage());
                        Thread.sleep(100);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to save cartels: " + e.getMessage());
        }
    }

    public FileConfiguration getCartelsConfig() {
        return cartelsConfig;
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