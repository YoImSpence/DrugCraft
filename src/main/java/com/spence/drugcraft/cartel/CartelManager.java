package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CartelManager {
    private final DrugCraft plugin;
    private final Map<String, Cartel> cartels = new HashMap<>();
    private final Map<String, Location> stashLocations = new HashMap<>();

    public CartelManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadCartels();
    }

    private void loadCartels() {
        File cartelFile = new File(plugin.getDataFolder(), "cartels.yml");
        if (!cartelFile.exists()) {
            plugin.saveResource("cartels.yml", false);
        }
        FileConfiguration cartelConfig = YamlConfiguration.loadConfiguration(cartelFile);
        ConfigurationSection cartelSection = cartelConfig.getConfigurationSection("cartels");
        if (cartelSection == null) {
            plugin.getLogger().warning("No cartels section found in cartels.yml");
            return;
        }
        for (String name : cartelSection.getKeys(false)) {
            ConfigurationSection cartelData = cartelSection.getConfigurationSection(name);
            if (cartelData != null) {
                String owner = cartelData.getString("owner");
                List<String> members = cartelData.getStringList("members");
                int stashLevel = cartelData.getInt("stashLevel", 1);
                int growthLevel = cartelData.getInt("growthLevel", 1);
                Location stashLocation = loadLocation(cartelData.getConfigurationSection("stashLocation"));
                Cartel cartel = new Cartel(name, UUID.fromString(owner));
                members.forEach(member -> cartel.addMember(UUID.fromString(member)));
                for (int i = 1; i < stashLevel; i++) cartel.upgradeStashLevel();
                for (int i = 1; i < growthLevel; i++) cartel.upgradeGrowthLevel();
                cartels.put(name, cartel);
                if (stashLocation != null) {
                    stashLocations.put(name, stashLocation);
                }
            }
        }
        plugin.getLogger().info("Loaded " + cartels.size() + " cartels");
    }

    public void saveCartels() {
        File cartelFile = new File(plugin.getDataFolder(), "cartels.yml");
        FileConfiguration cartelConfig = new YamlConfiguration();
        ConfigurationSection cartelSection = cartelConfig.createSection("cartels");
        for (Map.Entry<String, Cartel> entry : cartels.entrySet()) {
            String name = entry.getKey();
            Cartel cartel = entry.getValue();
            ConfigurationSection cartelData = cartelSection.createSection(name);
            cartelData.set("owner", cartel.getOwner().toString());
            cartelData.set("members", cartel.getMembers().stream().map(UUID::toString).toList());
            cartelData.set("stashLevel", cartel.getStashLevel());
            cartelData.set("growthLevel", cartel.getGrowthLevel());
            Location stashLocation = stashLocations.get(name);
            if (stashLocation != null) {
                ConfigurationSection locSection = cartelData.createSection("stashLocation");
                locSection.set("world", stashLocation.getWorld().getName());
                locSection.set("x", stashLocation.getX());
                locSection.set("y", stashLocation.getY());
                locSection.set("z", stashLocation.getZ());
                locSection.set("yaw", stashLocation.getYaw());
                locSection.set("pitch", stashLocation.getPitch());
            }
        }
        try {
            cartelConfig.save(cartelFile);
            plugin.getLogger().info("Saved " + cartels.size() + " cartels");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save cartels.yml: " + e.getMessage());
        }
    }

    public void setStashLocation(String cartelName, Location location) {
        stashLocations.put(cartelName, location);
        saveCartels();
    }

    public String getCartelByStashLocation(Location location) {
        return stashLocations.entrySet().stream()
                .filter(e -> e.getValue().equals(location))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public void removeStashLocation(String cartelName) {
        stashLocations.remove(cartelName);
        saveCartels();
    }

    private Location loadLocation(ConfigurationSection section) {
        if (section == null) return null;
        String worldName = section.getString("world");
        if (worldName == null) return null;
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String createCartel(Player player, String name) {
        if (cartels.containsKey(name)) {
            return "failed";
        }
        Cartel cartel = new Cartel(name, player.getUniqueId());
        cartels.put(name, cartel);
        saveCartels();
        return "success";
    }

    public Cartel getCartel(String name) {
        return cartels.get(name);
    }

    public String getPlayerCartel(UUID playerUUID) {
        return cartels.entrySet().stream()
                .filter(entry -> entry.getValue().getMembers().contains(playerUUID))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}