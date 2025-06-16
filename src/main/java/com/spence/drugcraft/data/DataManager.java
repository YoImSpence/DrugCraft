package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class DataManager {
    private final DrugCraft plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (!dataConfig.contains("players")) {
            dataConfig.createSection("players");
            saveData();
            plugin.getLogger().info("Initialized 'players' section in data.yml");
        }
    }

    public FileConfiguration getConfig() {
        return dataConfig;
    }

    public void saveConfig() {
        saveData();
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
            plugin.getLogger().info("Saved data to data.yml");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save data.yml: " + e.getMessage());
        }
    }

    public int getPlayerLevel(UUID playerUUID) {
        return dataConfig.getInt("players." + playerUUID + ".level", 1);
    }

    public void setPlayerLevel(UUID playerUUID, int level) {
        dataConfig.set("players." + playerUUID + ".level", level);
        saveData();
    }

    public long getXPRequiredForLevel(int level) {
        return dataConfig.getLong("levels." + level + ".xp-required", 100L * level);
    }

    public void addXP(UUID playerUUID, String skill, long xp) {
        long currentXP = dataConfig.getLong("players." + playerUUID + ".skills." + skill + ".xp", 0);
        dataConfig.set("players." + playerUUID + ".skills." + skill + ".xp", currentXP + xp);
        saveData();
    }

    public void resetPlayerXP(UUID playerUUID, String skill) {
        dataConfig.set("players." + playerUUID + ".skills." + skill + ".xp", 0);
        saveData();
    }

    public Map<String, Long> getPlayerDrugXPs(UUID playerUUID) {
        ConfigurationSection skillsSection = dataConfig.getConfigurationSection("players." + playerUUID + ".skills");
        Map<String, Long> drugXPs = new HashMap<>();
        if (skillsSection != null) {
            for (String drugId : skillsSection.getKeys(false)) {
                long xp = skillsSection.getLong(drugId + ".xp", 0);
                drugXPs.put(drugId, xp);
            }
        }
        return drugXPs;
    }

    public long getDrugXP(UUID playerUUID, String drugId) {
        return dataConfig.getLong("players." + playerUUID + ".skills." + drugId + ".xp", 0);
    }
}