package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final DrugCraft plugin;
    private FileConfiguration config;

    public ConfigManager(DrugCraft plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public double getWetFarmlandMultiplier() {
        return config.getDouble("farmland.wet_multiplier", 0.8);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}