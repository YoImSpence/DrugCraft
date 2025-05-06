package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final DrugCraft plugin;
    private FileConfiguration config;
    private FileConfiguration drugsConfig;
    private File configFile;
    private File drugsFile;

    public ConfigManager(DrugCraft plugin) {
        this.plugin = plugin;
        setupConfigs();
    }

    private void setupConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        drugsFile = new File(plugin.getDataFolder(), "drugs.yml");

        // Ensure drugs.yml exists
        if (!drugsFile.exists()) {
            plugin.getLogger().info("drugs.yml not found, copying default from resources...");
            saveResource("drugs.yml", false);
        }

        try {
            config = plugin.getConfig();
            drugsConfig = YamlConfiguration.loadConfiguration(drugsFile);
            // Validate drugs.yml
            if (!drugsConfig.contains("drugs")) {
                plugin.getLogger().warning("drugs.yml is missing 'drugs' section, attempting to restore default...");
                saveResource("drugs.yml", true); // Overwrite with default
                drugsConfig = YamlConfiguration.loadConfiguration(drugsFile);
            }
            // Validate growth_multiplier
            if (!config.contains("crops.growth_multiplier")) {
                config.set("crops.growth_multiplier", 1.0);
                saveConfig();
            } else {
                double multiplier = config.getDouble("crops.growth_multiplier");
                if (multiplier < 0.1 || multiplier > 10.0) {
                    plugin.getLogger().warning("Invalid crops.growth_multiplier: " + multiplier + ". Must be between 0.1 and 10.0. Using default 1.0.");
                    config.set("crops.growth_multiplier", 1.0);
                    saveConfig();
                }
            }
            // Validate wet_farmland_multiplier
            if (!config.contains("crops.wet_farmland_multiplier")) {
                config.set("crops.wet_farmland_multiplier", 0.75);
                saveConfig();
            } else {
                double wetMultiplier = config.getDouble("crops.wet_farmland_multiplier");
                if (wetMultiplier < 0.1 || wetMultiplier > 1.0) {
                    plugin.getLogger().warning("Invalid crops.wet_farmland_multiplier: " + wetMultiplier + ". Must be between 0.1 and 1.0. Using default 0.75.");
                    config.set("crops.wet_farmland_multiplier", 0.75);
                    saveConfig();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load configuration files: " + e.getMessage());
            drugsConfig = new YamlConfiguration(); // Fallback to empty config
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.getLogger().info("config.yml not found, copying default from resources...");
            plugin.saveResource("config.yml", false);
        }
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load config.yml: " + e.getMessage());
        }
    }

    public void saveResource(String resource, boolean replace) {
        File file = new File(plugin.getDataFolder(), resource);
        if (!file.exists() || replace) {
            try {
                plugin.saveResource(resource, replace);
                plugin.getLogger().info("Saved resource: " + resource);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save resource " + resource + ": " + e.getMessage());
            }
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getDrugsConfig() {
        return drugsConfig;
    }

    public double getGrowthMultiplier() {
        return config.getDouble("crops.growth_multiplier", 1.0);
    }

    public double getWetFarmlandMultiplier() {
        return config.getDouble("crops.wet_farmland_multiplier", 0.75);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config: " + e.getMessage());
        }
    }

    public void saveDrugsConfig() {
        try {
            drugsConfig.save(drugsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save drugs config: " + e.getMessage());
        }
    }
}