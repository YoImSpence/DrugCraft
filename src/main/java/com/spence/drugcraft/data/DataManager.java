package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
        initializeFiles();
    }

    private void initializeFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        cartelsFile = new File(plugin.getDataFolder(), "cartels.yml");
        if (!cropsFile.exists()) {
            plugin.saveResource("crops.yml", false);
        }
        if (!cartelsFile.exists()) {
            plugin.saveResource("cartels.yml", false);
        }
        cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
        cartelsConfig = YamlConfiguration.loadConfiguration(cartelsFile);
    }

    public void saveCrop(Crop crop) {
        String key = plugin.getCropManager().getLocationKey(crop.getLocation());
        ConfigurationSection section = cropsConfig.createSection("crops." + key);
        section.set("world", crop.getLocation().getWorld().getName());
        section.set("x", crop.getLocation().getBlockX());
        section.set("y", crop.getLocation().getBlockY());
        section.set("z", crop.getLocation().getBlockZ());
        section.set("drug_id", crop.getDrugId());
        section.set("planting_time", crop.getPlantingTime());
        section.set("age", crop.getAge());
        section.set("hologram_id", crop.getHologramId());
        try {
            cropsConfig.save(cropsFile);
            logger.fine("Saved crop " + crop.getDrugId() + " at " + key);
        } catch (IOException e) {
            logger.severe("Failed to save crops.yml: " + e.getMessage());
        }
    }

    public void saveCrops() {
        cropsConfig.set("crops", null); // Clear existing data
        for (Crop crop : plugin.getCropManager().getCrops().values()) {
            saveCrop(crop);
        }
        try {
            cropsConfig.save(cropsFile);
            logger.info("Saved " + plugin.getCropManager().getCrops().size() + " crops to crops.yml");
        } catch (IOException e) {
            logger.severe("Failed to save crops.yml: " + e.getMessage());
        }
    }

    public List<Crop> loadCrops() {
        List<Crop> loadedCrops = new ArrayList<>();
        ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
        if (cropsSection == null) {
            logger.info("No crops found in crops.yml");
            return loadedCrops;
        }
        for (String key : cropsSection.getKeys(false)) {
            ConfigurationSection section = cropsSection.getConfigurationSection(key);
            if (section == null) {
                logger.warning("Invalid crop configuration for key: " + key);
                continue;
            }
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                logger.warning("World not found for crop " + key + ": " + worldName);
                continue;
            }
            int x = section.getInt("x");
            int y = section.getInt("y");
            int z = section.getInt("z");
            String drugId = section.getString("drug_id");
            long plantingTime = section.getLong("planting_time");
            int age = section.getInt("age");
            String hologramId = section.getString("hologram_id");
            Location location = new Location(world, x, y, z);
            Crop crop = new Crop(location, drugId, plantingTime, age, hologramId);
            loadedCrops.add(crop);
            logger.fine("Loaded crop " + drugId + " at " + key);
        }
        logger.info("Loaded " + loadedCrops.size() + " crops from crops.yml");
        return loadedCrops;
    }

    public void removeCrop(Crop crop) {
        String key = plugin.getCropManager().getLocationKey(crop.getLocation());
        cropsConfig.set("crops." + key, null);
        try {
            cropsConfig.save(cropsFile);
            logger.fine("Removed crop " + crop.getDrugId() + " from crops.yml at " + key);
        } catch (IOException e) {
            logger.severe("Failed to save crops.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getCartelsConfig() {
        return cartelsConfig;
    }

    public void saveCartels() {
        try {
            cartelsConfig.save(cartelsFile);
            logger.info("Saved cartels to cartels.yml");
        } catch (IOException e) {
            logger.severe("Failed to save cartels.yml: " + e.getMessage());
        }
    }
}