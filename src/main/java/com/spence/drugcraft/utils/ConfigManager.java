package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigManager {
    private final DrugCraft plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();
    private final Logger logger = Logger.getLogger(ConfigManager.class.getName());

    public ConfigManager(DrugCraft plugin) {
        this.plugin = plugin;
        initializeConfigs();
    }

    private void initializeConfigs() {
        String[] configNames = {"data.yml", "cartels.yml", "drugs.yml", "unlocks.yml", "police.yml", "npcs.yml", "messages.yml"};
        for (String configName : configNames) {
            File file = new File(plugin.getDataFolder(), configName);
            if (!file.exists()) {
                plugin.saveResource(configName, false);
            }
            configFiles.put(configName, file);
            configs.put(configName, YamlConfiguration.loadConfiguration(file));
        }
    }

    public FileConfiguration getConfig(String configName) {
        FileConfiguration config = configs.get(configName);
        if (config == null) {
            logger.warning("Configuration file not found: " + configName);
            File file = new File(plugin.getDataFolder(), configName);
            config = YamlConfiguration.loadConfiguration(file);
            configs.put(configName, config);
            configFiles.put(configName, file);
        }
        return config;
    }

    public void saveConfig(String configName) {
        FileConfiguration config = configs.get(configName);
        File file = configFiles.get(configName);
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                logger.warning("Failed to save configuration file: " + configName + ": " + e.getMessage());
            }
        } else {
            logger.warning("Cannot save configuration file: " + configName + " - not loaded.");
        }
    }
}