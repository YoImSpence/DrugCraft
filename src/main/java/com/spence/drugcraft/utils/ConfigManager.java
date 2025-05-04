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
        config = plugin.getConfig();
        drugsConfig = YamlConfiguration.loadConfiguration(drugsFile);
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveResource(String resource, boolean replace) {
        if (!new File(plugin.getDataFolder(), resource).exists() || replace) {
            plugin.saveResource(resource, replace);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getDrugsConfig() {
        return drugsConfig;
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