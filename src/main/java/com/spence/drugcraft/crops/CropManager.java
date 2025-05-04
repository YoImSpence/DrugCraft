package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CropManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final DataManager dataManager;
    private final Map<String, Crop> crops = new HashMap<>();
    private final Logger logger;

    public CropManager(DrugCraft plugin, DrugManager drugManager, DataManager dataManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.dataManager = dataManager;
        this.logger = plugin.getLogger();
        startUpdateTask();
    }

    public void addCrop(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        crops.put(key, crop);
        dataManager.saveCrop(crop);
        String hologramId = "crop_" + key;
        Hologram hologram = DHAPI.createHologram(hologramId, crop.getLocation().add(0.5, 1, 0.5));
        DHAPI.setHologramLines(hologram, List.of(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("hologram.format", "&aGrowth: &e%percentage%%").replace("%percentage%", "0.00"))));
        crop.setHologramId(hologramId);
    }

    public Crop getCrop(Location location) {
        return crops.get(getLocationKey(location));
    }

    public void removeCrop(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        crops.remove(key);
        dataManager.removeCrop(crop);
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
            String format = plugin.getConfigManager().getConfig().getString("hologram.format", "&aGrowth: &e%percentage%%");
            for (Crop crop : crops.values()) {
                double growth = getGrowthPercentage(crop);
                Hologram hologram = DHAPI.getHologram(crop.getHologramId());
                if (hologram != null) {
                    DHAPI.setHologramLines(hologram, List.of(ChatColor.translateAlternateColorCodes('&', format.replace("%percentage%", String.format("%.2f", growth)))));
                }
            }
        }, 0L, 20L * plugin.getConfigManager().getConfig().getInt("hologram.update_interval", 5));
    }

    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }
}