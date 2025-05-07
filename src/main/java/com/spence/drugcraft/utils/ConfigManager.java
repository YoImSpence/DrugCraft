package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Logger;

public class ConfigManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private FileConfiguration config;

    public ConfigManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        logger.info("Loaded config.yml");
    }

    public void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            logger.info("Saved config.yml");
        } catch (Exception e) {
            logger.severe("Failed to save config.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public double getWetFarmlandMultiplier() {
        return config.getDouble("farmland.wet_multiplier", 0.8);
    }
}