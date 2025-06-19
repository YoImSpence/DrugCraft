package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final DrugCraft plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final String[] configFiles = {
            "businesses.yml", "cartels.yml", "casino.yml", "citizens.yml", "config.yml",
            "crops.yml", "data.yml", "drugs.yml", "games.yml", "messages.yml",
            "player.yml", "police.yml", "server.yml", "steeds.yml", "town.yml",
            "vehicles.yml", "heists.yml"
    };

    public ConfigManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        for (String fileName : configFiles) {
            File file = new File(plugin.getDataFolder(), fileName);
            if (!file.exists()) {
                plugin.saveResource(fileName, false);
            }
            configs.put(fileName, YamlConfiguration.loadConfiguration(file));
        }
    }

    public FileConfiguration getConfig(String fileName) {
        return configs.getOrDefault(fileName, new YamlConfiguration());
    }

    public void reloadConfigs() {
        configs.clear();
        loadConfigs();
    }

    public void saveConfig(String fileName) {
        try {
            File file = new File(plugin.getDataFolder(), fileName);
            configs.get(fileName).save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save " + fileName + ": " + e.getMessage());
        }
    }
}