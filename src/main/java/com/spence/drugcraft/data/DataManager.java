package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.PlayerAddictionData;
import com.spence.drugcraft.crops.Crop;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DataManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private final File cropsFile;
    private final File addictionFile;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        this.addictionFile = new File(plugin.getDataFolder(), "addiction.yml");
    }

    public void saveCrop(Crop crop) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(cropsFile);
        String key = getLocationKey(crop.getLocation());
        config.set(key + ".world", crop.getLocation().getWorld().getName());
        config.set(key + ".x", crop.getLocation().getX());
        config.set(key + ".y", crop.getLocation().getY());
        config.set(key + ".z", crop.getLocation().getZ());
        config.set(key + ".drug_id", crop.getDrugId());
        config.set(key + ".planting_time", crop.getPlantingTime());
        config.set(key + ".hologram_id", crop.getHologramId());
        try {
            config.save(cropsFile);
        } catch (Exception e) {
            logger.severe("Failed to save crop: " + e.getMessage());
        }
    }

    public void removeCrop(Crop crop) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(cropsFile);
        String key = getLocationKey(crop.getLocation());
        config.set(key, null);
        try {
            config.save(cropsFile);
        } catch (Exception e) {
            logger.severe("Failed to remove crop: " + e.getMessage());
        }
    }

    public void loadCrops(Map<String, Crop> crops) {
        if (!cropsFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(cropsFile);
        for (String key : config.getKeys(false)) {
            String world = config.getString(key + ".world");
            double x = config.getDouble(key + ".x");
            double y = config.getDouble(key + ".y");
            double z = config.getDouble(key + ".z");
            String drugId = config.getString(key + ".drug_id");
            long plantingTime = config.getLong(key + ".planting_time");
            String hologramId = config.getString(key + ".hologram_id");
            Location location = new Location(plugin.getServer().getWorld(world), x, y, z);
            Crop crop = new Crop(location, drugId, plantingTime);
            crop.setHologramId(hologramId);
            crops.put(key, crop);
        }
    }

    public void savePlayerData(UUID uuid, PlayerAddictionData data) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(addictionFile);
        String path = "players." + uuid.toString();
        for (Map.Entry<String, Integer> entry : data.getUsesMap().entrySet()) {
            config.set(path + "." + entry.getKey() + ".uses", entry.getValue());
        }
        for (Map.Entry<String, Long> entry : data.getLastUseMap().entrySet()) {
            config.set(path + "." + entry.getKey() + ".last_use", entry.getValue());
        }
        try {
            config.save(addictionFile);
        } catch (Exception e) {
            logger.severe("Failed to save player data: " + e.getMessage());
        }
    }

    public void loadPlayerData(Map<UUID, PlayerAddictionData> playerData) {
        if (!addictionFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(addictionFile);
        ConfigurationSection players = config.getConfigurationSection("players");
        if (players != null) {
            for (String uuidStr : players.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerAddictionData data = new PlayerAddictionData();
                ConfigurationSection drugs = players.getConfigurationSection(uuidStr);
                if (drugs != null) {
                    for (String drugId : drugs.getKeys(false)) {
                        int uses = drugs.getInt(drugId + ".uses");
                        long lastUse = drugs.getLong(drugId + ".last_use");
                        data.getUsesMap().put(drugId, uses);
                        data.getLastUseMap().put(drugId, lastUse);
                    }
                }
                playerData.put(uuid, data);
            }
        }
    }

    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }
}