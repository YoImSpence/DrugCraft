package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TownConfig {
    private final DrugCraft plugin;
    private final FileConfiguration config;

    public TownConfig(DrugCraft plugin) {
        this.plugin = plugin;
        File configFile = new File(plugin.getDataFolder(), "town.yml");
        if (!configFile.exists()) {
            plugin.saveResource("town.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadConfig();
    }

    private void loadConfig() {
        config.options().copyDefaults(true);
        config.addDefault("town.spawn_locations", new ArrayList<>());
        config.addDefault("town.meetup_spots", new ArrayList<>());
        config.addDefault("town.max_citizens", 3);
        try {
            config.save(new File(plugin.getDataFolder(), "town.yml"));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save town.yml: " + e.getMessage());
        }
    }

    public List<Location> getCitizenSpawnLocations() {
        List<Location> locations = new ArrayList<>();
        ConfigurationSection spawnSection = config.getConfigurationSection("town.spawn_locations");
        if (spawnSection == null || spawnSection.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("No spawn locations defined in town.yml.");
            return locations;
        }
        plugin.getLogger().info("Found " + spawnSection.getKeys(false).size() + " spawn locations in town.yml");
        for (String key : spawnSection.getKeys(false)) {
            ConfigurationSection locSection = spawnSection.getConfigurationSection(key);
            if (locSection == null) {
                plugin.getLogger().warning("Invalid spawn location entry for key: " + key);
                continue;
            }
            String worldName = locSection.getString("world");
            if (worldName == null) {
                plugin.getLogger().warning("No world specified for spawn location: " + key);
                continue;
            }
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("Invalid world in town.yml for location " + key + ": " + worldName + ". Available worlds: " + org.bukkit.Bukkit.getWorlds().stream().map(org.bukkit.World::getName).collect(Collectors.joining(", ")));
                continue;
            }
            try {
                double x = locSection.getDouble("x");
                double y = locSection.getDouble("y");
                double z = locSection.getDouble("z");
                float yaw = (float) locSection.getDouble("yaw", 0.0);
                float pitch = (float) locSection.getDouble("pitch", 0.0);
                locations.add(new Location(world, x, y, z, yaw, pitch));
                plugin.getLogger().info("Successfully loaded spawn location " + key + ": " + worldName + " (" + x + ", " + y + ", " + z + ")");
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid location format in town.yml for " + key + ": " + e.getMessage());
            }
        }
        return locations;
    }

    public List<Location> getMeetupSpots() {
        List<Location> locations = new ArrayList<>();
        ConfigurationSection meetupSection = config.getConfigurationSection("town.meetup_spots");
        if (meetupSection == null || meetupSection.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("No meetup spots defined in town.yml.");
            return locations;
        }
        plugin.getLogger().info("Found " + meetupSection.getKeys(false).size() + " meetup spots in town.yml");
        for (String key : meetupSection.getKeys(false)) {
            ConfigurationSection locSection = meetupSection.getConfigurationSection(key);
            if (locSection == null) {
                plugin.getLogger().warning("Invalid meetup spot entry for key: " + key);
                continue;
            }
            String worldName = locSection.getString("world");
            if (worldName == null) {
                plugin.getLogger().warning("No world specified for meetup spot: " + key);
                continue;
            }
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("Invalid world in town.yml for meetup spot " + key + ": " + worldName + ". Available worlds: " + org.bukkit.Bukkit.getWorlds().stream().map(org.bukkit.World::getName).collect(Collectors.joining(", ")));
                continue;
            }
            try {
                double x = locSection.getDouble("x");
                double y = locSection.getDouble("y");
                double z = locSection.getDouble("z");
                float yaw = (float) locSection.getDouble("yaw", 0.0);
                float pitch = (float) locSection.getDouble("pitch", 0.0);
                locations.add(new Location(world, x, y, z, yaw, pitch));
                plugin.getLogger().info("Successfully loaded meetup spot " + key + ": " + worldName + " (" + x + ", " + y + ", " + z + ")");
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid location format in town.yml for meetup spot " + key + ": " + e.getMessage());
            }
        }
        return locations;
    }

    public int getMaxCitizens() {
        return config.getInt("town.max_citizens", 3);
    }
}