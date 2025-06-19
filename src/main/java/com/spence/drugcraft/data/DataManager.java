package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class DataManager {
    private final DrugCraft plugin;
    private final FileConfiguration dataConfig;

    public DataManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.dataConfig = plugin.getConfig("data.yml");
    }

    public int getPlayerLevel(UUID playerUUID) {
        return dataConfig.getInt("players." + playerUUID + ".level", 0);
    }

    public void setPlayerLevel(UUID playerUUID, int level) {
        dataConfig.set("players." + playerUUID + ".level", level);
        plugin.saveConfig();
    }

    public long getPlayerDrugXP(UUID playerUUID, String skill) {
        return dataConfig.getLong("players." + playerUUID + ".skills." + skill + ".xp", 0);
    }

    public long getXPRequiredForLevel(int level) {
        return level * 1000L; // Configurable XP curve
    }

    public void saveAddiction(UUID playerUUID, String drugId, double level, long lastUse) {
        dataConfig.set("players." + playerUUID + ".addictions." + drugId + ".level", level);
        dataConfig.set("players." + playerUUID + ".addictions." + drugId + ".lastUse", lastUse);
        plugin.saveConfig();
    }

    public void removeAddiction(UUID playerUUID, String drugId) {
        dataConfig.set("players." + playerUUID + ".addictions." + drugId, null);
        plugin.saveConfig();
    }

    public void resetPlayerDrugXP(UUID playerUUID, String skill) {
        dataConfig.set("players." + playerUUID + ".skills." + skill + ".xp", 0);
        plugin.saveConfig();
    }

    public void addPlayerDrugXP(UUID playerUUID, String skill, long xp) {
        long currentXP = dataConfig.getLong("players." + playerUUID + ".skills." + skill + ".xp", 0);
        dataConfig.set("players." + playerUUID + ".skills." + skill + ".xp", currentXP + xp);
        plugin.saveConfig();
    }
}