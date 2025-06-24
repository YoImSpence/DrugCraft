package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataManager {
    private final DrugCraft plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveAddiction(UUID playerUUID, String drugId, double severity, long lastUse) {
        String path = "players." + playerUUID + ".addictions." + drugId;
        dataConfig.set(path + ".severity", severity);
        dataConfig.set(path + ".lastUse", lastUse);
        saveConfig();
    }

    public void removeAddiction(UUID playerUUID, String drugId) {
        String path = "players." + playerUUID + ".addictions." + drugId;
        dataConfig.set(path, null);
        saveConfig();
    }

    public int getPlayerLevel(UUID playerUUID) {
        return dataConfig.getInt("players." + playerUUID + ".level", 1);
    }

    public int getPlayerXP(UUID playerUUID) {
        return dataConfig.getInt("players." + playerUUID + ".xp", 0);
    }

    // Stub methods to fix compilation errors
    public double getAddictionSeverity(UUID playerUUID, String drugId) {
        String path = "players." + playerUUID + ".addictions." + drugId + ".severity";
        return dataConfig.getDouble(path, 0.0);
    }

    public List<String> getUnlockedDrugs(UUID playerUUID) {
        return new ArrayList<>(); // Placeholder
    }

    public List<String> getUnlockedFeatures(UUID playerUUID) {
        return new ArrayList<>(); // Placeholder
    }

    private void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data.yml: " + e.getMessage());
        }
    }
}