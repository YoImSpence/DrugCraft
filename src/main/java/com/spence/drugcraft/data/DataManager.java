package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.houses.HouseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DataManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private final File cartelsFile;
    private final File cropsFile;
    private final File addictionFile;
    private FileConfiguration cartelsConfig;
    private FileConfiguration cropsConfig;
    private FileConfiguration addictionConfig;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.cartelsFile = new File(plugin.getDataFolder(), "cartels.yml");
        this.cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        this.addictionFile = new File(plugin.getDataFolder(), "addiction.yml");

        if (!cartelsFile.exists()) {
            plugin.saveResource("cartels.yml", false);
        }
        if (!cropsFile.exists()) {
            plugin.saveResource("crops.yml", false);
        }
        if (!addictionFile.exists()) {
            plugin.saveResource("addiction.yml", false);
        }

        this.cartelsConfig = YamlConfiguration.loadConfiguration(cartelsFile);
        this.cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
        this.addictionConfig = YamlConfiguration.loadConfiguration(addictionFile);
    }

    public void loadCrops() {
        ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
        if (cropsSection == null) {
            logger.info("No crops found in crops.yml");
            return;
        }
        for (String key : cropsSection.getKeys(false)) {
            try {
                ConfigurationSection cropSection = cropsSection.getConfigurationSection(key);
                if (cropSection == null) continue;
                String drugId = cropSection.getString("drug_id");
                String worldName = cropSection.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    logger.warning("World not found for crop at " + key + ": " + worldName);
                    continue;
                }
                int x = cropSection.getInt("x");
                int y = cropSection.getInt("y");
                int z = cropSection.getInt("z");
                Location location = new Location(world, x, y, z);
                UUID playerUUID = UUID.fromString(cropSection.getString("player_uuid"));
                long plantingTime = cropSection.getLong("planting_time");
                String quality = cropSection.getString("quality", "Basic");
                Crop crop = new Crop(drugId, location, playerUUID, plantingTime, quality);
                plugin.getCropManager().addCrop(crop);
                logger.fine("Loaded crop: " + drugId + " at " + key);
            } catch (IllegalArgumentException e) {
                logger.warning("Failed to load crop " + key + ": " + e.getMessage());
            }
        }
        logger.info("Loaded " + plugin.getCropManager().getCrops().size() + " crops");
    }

    public FileConfiguration getCartelsConfig() {
        return cartelsConfig;
    }

    public FileConfiguration getCropsConfig() {
        return cropsConfig;
    }

    public FileConfiguration getAddictionConfig() {
        return addictionConfig;
    }

    public void saveCartels() {
        try {
            cartelsConfig.save(cartelsFile);
        } catch (IOException e) {
            logger.severe("Failed to save cartels.yml: " + e.getMessage());
        }
    }

    public void saveCrops() {
        try {
            cropsConfig.save(cropsFile);
        } catch (IOException e) {
            logger.severe("Failed to save crops.yml: " + e.getMessage());
        }
    }

    public void saveAddiction() {
        try {
            addictionConfig.save(addictionFile);
        } catch (IOException e) {
            logger.severe("Failed to save addiction.yml: " + e.getMessage());
        }
    }

    public void saveAll() {
        saveCartels();
        saveCrops();
        saveAddiction();
        plugin.getHouseManager().saveHouses();
    }

    public void saveCrop(Crop crop) {
        String key = crop.getLocation().getWorld().getName() + "_" + crop.getLocation().getBlockX() + "_" +
                crop.getLocation().getBlockY() + "_" + crop.getLocation().getBlockZ();
        String path = "crops." + key;
        cropsConfig.set(path + ".drug_id", crop.getDrugId());
        cropsConfig.set(path + ".world", crop.getLocation().getWorld().getName());
        cropsConfig.set(path + ".x", crop.getLocation().getBlockX());
        cropsConfig.set(path + ".y", crop.getLocation().getBlockY());
        cropsConfig.set(path + ".z", crop.getLocation().getBlockZ());
        cropsConfig.set(path + ".player_uuid", crop.getPlayerUUID().toString());
        cropsConfig.set(path + ".planting_time", crop.getPlantingTime());
        cropsConfig.set(path + ".quality", crop.getQuality());
        saveCrops();
    }

    public void removeCrop(Crop crop) {
        String key = crop.getLocation().getWorld().getName() + "_" + crop.getLocation().getBlockX() + "_" +
                crop.getLocation().getBlockY() + "_" + crop.getLocation().getBlockZ();
        cropsConfig.set("crops." + key, null);
        saveCrops();
    }

    public void saveStash(String cartelName, Map<String, Object> stash) {
        ConfigurationSection stashSection = cartelsConfig.createSection("cartels." + cartelName + ".stash");
        for (Map.Entry<String, Object> entry : stash.entrySet()) {
            stashSection.set(entry.getKey(), entry.getValue());
        }
        saveCartels();
    }
}