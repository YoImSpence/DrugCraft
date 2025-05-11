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
    private FileConfiguration cartelsConfig;
    private File configFile;
    private File drugsFile;
    private File cartelsFile;

    public ConfigManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadConfig();
        loadDrugsConfig();
        loadCartelsConfig();
    }

    private void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void loadDrugsConfig() {
        drugsFile = new File(plugin.getDataFolder(), "drugs.yml");
        if (!drugsFile.exists()) {
            plugin.saveResource("drugs.yml", false);
        }
        drugsConfig = YamlConfiguration.loadConfiguration(drugsFile);
    }

    private void loadCartelsConfig() {
        cartelsFile = new File(plugin.getDataFolder(), "cartels.yml");
        if (!cartelsFile.exists()) {
            plugin.saveResource("cartels.yml", false);
        }
        cartelsConfig = YamlConfiguration.loadConfiguration(cartelsFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getDrugsConfig() {
        return drugsConfig;
    }

    public FileConfiguration getCartelsConfig() {
        return cartelsConfig;
    }

    public double getWetFarmlandMultiplier() {
        return config.getDouble("crops.wet_farmland_multiplier", 0.8);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config.yml: " + e.getMessage());
        }
    }

    public void saveDrugsConfig() {
        try {
            drugsConfig.save(drugsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save drugs.yml: " + e.getMessage());
        }
    }

    public void saveCartelsConfig() {
        try {
            cartelsConfig.save(cartelsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save cartels.yml: " + e.getMessage());
        }
    }
}