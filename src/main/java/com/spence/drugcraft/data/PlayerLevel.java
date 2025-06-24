package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.UUID;

public class PlayerLevel {
    private final DrugCraft plugin;

    public PlayerLevel(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public int getPlayerLevel(UUID playerUUID) {
        return plugin.getDataManager().getPlayerLevel(playerUUID);
    }

    public List<String> getUnlockedDrugs(int level) {
        ConfigurationSection levelConfig = plugin.getConfigManager().getConfig("unlocks.yml").getConfigurationSection("levels." + level);
        if (levelConfig == null) return List.of();
        return levelConfig.getStringList("drugs");
    }

    public List<String> getUnlockedFeatures(int level) {
        ConfigurationSection levelConfig = plugin.getConfigManager().getConfig("unlocks.yml").getConfigurationSection("levels." + level);
        if (levelConfig == null) return List.of();
        return levelConfig.getStringList("features");
    }
}