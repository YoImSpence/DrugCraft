package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.cartel.CartelManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
    private File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        initializeFiles();
    }

    private void initializeFiles() {
        cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        cartelsFile = new File(plugin.getDataFolder(), "cartels.yml");
        dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!cropsFile.exists()) {
            plugin.saveResource("crops.yml", false);
        }
        if (!cartelsFile.exists()) {
            plugin.saveResource("cartels.yml", false);
        }
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }

        cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
        cartelsConfig = YamlConfiguration.loadConfiguration(cartelsFile);
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveCrop(Crop crop) {
        String key = crop.getLocation().getWorld().getName() + "_" +
                crop.getLocation().getBlockX() + "_" +
                crop.getLocation().getBlockY() + "_" +
                crop.getLocation().getBlockZ();
        ConfigurationSection section = cropsConfig.createSection("crops." + key);
        section.set("drug_id", crop.getDrugId());
        section.set("planting_time", crop.getPlantingTime());
        section.set("age", crop.getAge());
        section.set("hologram_id", crop.getHologramId());
        saveCropsConfig();
    }

    public void removeCrop(Crop crop) {
        String key = crop.getLocation().getWorld().getName() + "_" +
                crop.getLocation().getBlockX() + "_" +
                crop.getLocation().getBlockY() + "_" +
                crop.getLocation().getBlockZ();
        cropsConfig.set("crops." + key, null);
        saveCropsConfig();
    }

    public List<Crop> loadCrops() {
        List<Crop> crops = new ArrayList<>();
        ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
        if (cropsSection == null) {
            logger.info("No crops found in crops.yml");
            return crops;
        }
        for (String key : cropsSection.getKeys(false)) {
            ConfigurationSection section = cropsSection.getConfigurationSection(key);
            String[] parts = key.split("_");
            if (parts.length != 4) {
                logger.warning("Invalid crop key format: " + key);
                continue;
            }
            try {
                Location location = new Location(
                        plugin.getServer().getWorld(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3])
                );
                String drugId = section.getString("drug_id");
                long plantingTime = section.getLong("planting_time");
                int age = section.getInt("age");
                String hologramId = section.getString("hologram_id");
                Crop crop = new Crop(location, drugId, plantingTime, age, hologramId);
                crops.add(crop);
            } catch (NumberFormatException e) {
                logger.warning("Invalid coordinates in crop key: " + key);
            }
        }
        logger.info("Loaded " + crops.size() + " crops from crops.yml");
        return crops;
    }

    public void saveCrops() {
        cropsConfig.set("crops", null);
        for (Crop crop : plugin.getCropManager().getCrops().values()) {
            saveCrop(crop);
        }
        logger.info("Saved " + plugin.getCropManager().getCrops().size() + " crops to crops.yml");
    }

    public FileConfiguration getCartelsConfig() {
        return cartelsConfig;
    }

    public void saveCartels() {
        ConfigurationSection cartelsSection = cartelsConfig.createSection("cartels");
        for (CartelManager.Cartel cartel : plugin.getCartelManager().getCartels().values()) {
            ConfigurationSection cartelSection = cartelsSection.createSection(cartel.getName());
            cartelSection.set("leader", cartel.getLeader().toString());
            cartelSection.set("members", cartel.getMembers().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList()));
            cartelSection.set("level", cartel.getLevel());
            cartelSection.set("stashed_money", cartel.getStashedMoney());
            ConfigurationSection permsSection = cartelSection.createSection("permissions");
            for (Map.Entry<UUID, Map<String, Boolean>> permEntry : cartel.getPermissions().entrySet()) {
                permsSection.set(permEntry.getKey().toString(), permEntry.getValue());
            }
            cartelSection.set("upgrades", cartel.getUpgrades());
            cartelSection.set("stash", cartel.getStash());
        }
        saveCartelsConfig();
        logger.info("Saved " + plugin.getCartelManager().getCartels().size() + " cartels to cartels.yml");
    }

    public void saveShutdownTime() {
        dataConfig.set("last_shutdown", System.currentTimeMillis());
        try {
            dataConfig.save(dataFile);
            logger.fine("Saved shutdown time to data.yml");
        } catch (IOException e) {
            logger.severe("Failed to save shutdown time to data.yml: " + e.getMessage());
        }
    }

    public long getOfflineTime() {
        long lastShutdown = dataConfig.getLong("last_shutdown", 0);
        if (lastShutdown == 0) {
            logger.info("No previous shutdown time found, assuming no offline time");
            return 0;
        }
        long offlineTime = System.currentTimeMillis() - lastShutdown;
        logger.info("Calculated offline time: " + offlineTime + "ms");
        return offlineTime;
    }

    public void saveStash(String cartelName, Map<String, Object> stash) {
        cartelsConfig.set("cartels." + cartelName + ".stash", stash);
        saveCartelsConfig();
    }

    private void saveCropsConfig() {
        try {
            cropsConfig.save(cropsFile);
        } catch (IOException e) {
            logger.severe("Failed to save crops.yml: " + e.getMessage());
        }
    }

    private void saveCartelsConfig() {
        try {
            cartelsConfig.save(cartelsFile);
        } catch (IOException e) {
            logger.severe("Failed to save cartels.yml: " + e.getMessage());
        }
    }
}