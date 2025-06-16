package com.spence.drugcraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PoliceConfig {
    private final File configFile;
    private FileConfiguration config;
    private final Random random = new Random();

    public PoliceConfig(File configFile) {
        this.configFile = configFile;
        loadConfig();
    }

    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        if (!configFile.exists()) {
            // Set default values
            config.set("spawn_locations.spawn1.world", "Greenfield");
            config.set("spawn_locations.spawn1.x", 0);
            config.set("spawn_locations.spawn1.y", 120);
            config.set("spawn_locations.spawn1.z", 0);
            config.set("spawn_locations.spawn1.yaw", 0);
            config.set("spawn_locations.spawn1.pitch", 0);

            config.set("patrol_locations.patrol1.world", "Greenfield");
            config.set("patrol_locations.patrol1.x", 10);
            config.set("patrol_locations.patrol1.y", 120);
            config.set("patrol_locations.patrol1.z", 10);
            config.set("patrol_locations.patrol1.yaw", 0);
            config.set("patrol_locations.patrol1.pitch", 0);

            config.set("fine_amount", 500.0);
            config.set("jail_location.world", "Greenfield");
            config.set("jail_location.x", 0);
            config.set("jail_location.y", 120);
            config.set("jail_location.z", 0);
            config.set("jail_location.yaw", 0);
            config.set("jail_location.pitch", 0);

            saveConfig();
        }
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public List<Location> getSpawnLocations() {
        List<Location> locations = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("spawn_locations");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection locSection = section.getConfigurationSection(key);
                if (locSection == null) continue;
                String worldName = locSection.getString("world");
                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                double x = locSection.getDouble("x");
                double y = locSection.getDouble("y");
                double z = locSection.getDouble("z");
                float yaw = (float) locSection.getDouble("yaw");
                float pitch = (float) locSection.getDouble("pitch");
                locations.add(new Location(world, x, y, z, yaw, pitch));
            }
        }
        return locations;
    }

    public List<Location> getPatrolLocations() {
        List<Location> locations = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("patrol_locations");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection locSection = section.getConfigurationSection(key);
                if (locSection == null) continue;
                String worldName = locSection.getString("world");
                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                double x = locSection.getDouble("x");
                double y = locSection.getDouble("y");
                double z = locSection.getDouble("z");
                float yaw = (float) locSection.getDouble("yaw");
                float pitch = (float) locSection.getDouble("pitch");
                locations.add(new Location(world, x, y, z, yaw, pitch));
            }
        }
        return locations;
    }

    public Location getRandomPatrolLocation() {
        List<Location> locations = getPatrolLocations();
        if (locations.isEmpty()) return null;
        return locations.get(random.nextInt(locations.size()));
    }

    public double getFineAmount() {
        return config.getDouble("fine_amount", 500.0);
    }

    public Location getJailLocation() {
        ConfigurationSection section = config.getConfigurationSection("jail_location");
        if (section == null) return null;
        String worldName = section.getString("world");
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
}