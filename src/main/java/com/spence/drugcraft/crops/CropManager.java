package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CropManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Map<String, Crop> crops = new HashMap<>();
    private final Logger logger;

    public CropManager(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.logger = plugin.getLogger();
        startUpdateTask();
    }

    public void addCrop(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        crops.put(key, crop);
    }

    public Crop getCrop(Location location) {
        return crops.get(getLocationKey(location));
    }

    public void removeCrop(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        crops.remove(key);
        if (crop.getHologramId() != null) {
            Hologram hologram = DHAPI.getHologram(crop.getHologramId());
            if (hologram != null) {
                hologram.delete();
            }
        }
    }

    public double getGrowthPercentage(Crop crop) {
        Drug drug = drugManager.getDrug(crop.getDrugId());
        if (drug == null) return 0;
        long timeElapsed = (System.currentTimeMillis() - crop.getPlantingTime()) / 1000;
        double percentage = (double) timeElapsed / drug.getGrowthTime() * 100;
        return Math.min(percentage, 100);
    }

    private void startUpdateTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            String format = plugin.getConfig().getString("hologram.format", "Growth: %percentage%%");
            for (Crop crop : crops.values()) {
                double growth = getGrowthPercentage(crop);
                Hologram hologram = DHAPI.getHologram(crop.getHologramId());
                if (hologram != null) {
                    DHAPI.setHologramLines(hologram, List.of(format.replace("%percentage%", String.format("%.2f", growth))));
                }
            }
        }, 0L, 20L * plugin.getConfig().getInt("hologram.update_interval", 5));
    }

    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    public void loadCrops() {
        File cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        if (!cropsFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(cropsFile);
        for (String key : config.getKeys(false)) {
            String world = config.getString(key + ".world");
            double x = config.getDouble(key + ".x");
            double y = config.getDouble(key + ".y");
            double z = config.getDouble(key + ".z");
            String drugId = config.getString(key + ".drug_id");
            long plantingTime = config.getLong(key + ".planting_time");
            String hologramId = config.getString(key + ".hologram_id");
            Location location = new Location(plugin.getServer().getWorld(world), x, y, z);
            Crop crop = new Crop(location, drugId, plantingTime);
            crop.setHologramId(hologramId);
            crops.put(key, crop);
        }
    }

    public void saveCrops() {
        File cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Crop> entry : crops.entrySet()) {
            String key = entry.getKey();
            Crop crop = entry.getValue();
            config.set(key + ".world", crop.getLocation().getWorld().getName());
            config.set(key + ".x", crop.getLocation().getX());
            config.set(key + ".y", crop.getLocation().getY());
            config.set(key + ".z", crop.getLocation().getZ());
            config.set(key + ".drug_id", crop.getDrugId());
            config.set(key + ".planting_time", crop.getPlantingTime());
            config.set(key + ".hologram_id", crop.getHologramId());
        }
        try {
            config.save(cropsFile);
        } catch (Exception e) {
            logger.severe("Failed to save crops: " + e.getMessage());
        }
    }
}