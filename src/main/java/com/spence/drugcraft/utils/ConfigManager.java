package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private final DrugCraft plugin;
    private FileConfiguration config;

    public ConfigManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        plugin.saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public List<Integer> getDealerItemSlots() {
        List<Integer> slots = new ArrayList<>();
        slots.addAll(config.getIntegerList("dealer.item_slots"));
        if (slots.isEmpty()) {
            // Default slots if not configured
            for (int i = 10; i <= 16; i++) slots.add(i);
            for (int i = 19; i <= 25; i++) slots.add(i);
            for (int i = 28; i <= 34; i++) slots.add(i);
        }
        return slots;
    }

    public int getDealerMainMenuSize() {
        return config.getInt("dealer.main_menu_size", 27);
    }

    public int getDealerBuyMenuSize() {
        return config.getInt("dealer.buy_menu_size", 54);
    }

    public List<Location> getPoliceSpawnLocations() {
        List<Location> locations = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("police.spawn_locations");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection locSection = section.getConfigurationSection(key);
                if (locSection != null) {
                    String worldName = locSection.getString("world");
                    if (worldName == null || plugin.getServer().getWorld(worldName) == null) continue;
                    Location location = new Location(
                            plugin.getServer().getWorld(worldName),
                            locSection.getDouble("x"),
                            locSection.getDouble("y"),
                            locSection.getDouble("z"),
                            (float) locSection.getDouble("yaw"),
                            (float) locSection.getDouble("pitch")
                    );
                    locations.add(location);
                }
            }
        }
        return locations;
    }
}