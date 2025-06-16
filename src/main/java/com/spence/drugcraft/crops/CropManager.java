package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class CropManager {
    private final DrugCraft plugin;
    private final Map<Location, Crop> crops;

    public CropManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.crops = new HashMap<>();
        loadCrops();
        startGrowthTask();
    }

    private void loadCrops() {
        File cropFile = new File(plugin.getDataFolder(), "crops.yml");
        if (!cropFile.exists()) {
            plugin.saveResource("crops.yml", false);
        }
        FileConfiguration cropConfig = YamlConfiguration.loadConfiguration(cropFile);
        ConfigurationSection cropSection = cropConfig.getConfigurationSection("crops");
        if (cropSection == null) {
            plugin.getLogger().warning("No 'crops' section found in crops.yml");
            return;
        }
        for (String key : cropSection.getKeys(false)) {
            ConfigurationSection cropData = cropSection.getConfigurationSection(key);
            if (cropData != null) {
                String[] coords = key.split(",");
                Location location = new Location(plugin.getServer().getWorld(cropData.getString("world")),
                        Double.parseDouble(coords[0]),
                        Double.parseDouble(coords[1]),
                        Double.parseDouble(coords[2]));
                String drugId = cropData.getString("drugId", "cannabis");
                String quality = cropData.getString("quality", "Standard");
                long plantedTime = cropData.getLong("plantedTime", System.currentTimeMillis());
                crops.put(location, new Crop(drugId, quality, plantedTime));
                plugin.getLogger().info("Loaded crop at " + location + ": " + drugId + " (" + quality + ")");
            }
        }
    }

    public void saveCrops() {
        File cropFile = new File(plugin.getDataFolder(), "crops.yml");
        FileConfiguration cropConfig = new YamlConfiguration();
        ConfigurationSection cropSection = cropConfig.createSection("crops");
        for (Map.Entry<Location, Crop> entry : crops.entrySet()) {
            Location location = entry.getKey();
            Crop crop = entry.getValue();
            String key = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
            ConfigurationSection cropData = cropSection.createSection(key);
            cropData.set("world", location.getWorld().getName());
            cropData.set("drugId", crop.getDrugId());
            cropData.set("quality", crop.getQuality());
            cropData.set("plantedTime", crop.getPlantedTime());
        }
        try {
            cropConfig.save(cropFile);
            plugin.getLogger().info("Saved " + crops.size() + " crops to crops.yml");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save crops.yml: " + e.getMessage());
        }
    }

    private void startGrowthTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, Crop> entry : crops.entrySet()) {
                    Location location = entry.getKey();
                    Crop crop = entry.getValue();
                    Block block = location.getBlock();
                    if (!isDrugCrop(block)) {
                        removeCrop(location);
                        plugin.getLogger().warning("Removed invalid crop at " + location);
                        continue;
                    }
                    double growth = getCurrentGrowth(crop);
                    boolean hasGrowLight = false;
                    for (int x = -2; x <= 2; x++) {
                        for (int y = -2; y <= 2; y++) {
                            for (int z = -2; z <= 2; z++) {
                                Block nearby = block.getRelative(x, y, z);
                                if (plugin.getGrowLight().isGrowLightBlock(nearby)) {
                                    hasGrowLight = true;
                                    break;
                                }
                            }
                            if (hasGrowLight) break;
                        }
                        if (hasGrowLight) break;
                    }
                    if (hasGrowLight) {
                        growth = Math.min(growth * 1.33, 100.0);
                    }
                    if (growth >= 100.0) {
                        block.setType(Material.WHEAT);
                        block.setBlockData(Bukkit.createBlockData(Material.WHEAT, "[age=7]"));
                    }
                    plugin.getLogger().info("Updated crop at " + location + ": " + crop.getDrugId() + " (" + crop.getQuality() + ") growth: " + growth + "%");
                }
                saveCrops();
            }
        }.runTaskTimer(plugin, 0L, 1200L);
    }

    public void plantCrop(Location location, ItemStack seed) {
        NBTBlock nbtBlock = new NBTBlock(location.getBlock());
        String drugId = nbtBlock.getData().getString("drug_id");
        String quality = nbtBlock.getData().getString("quality");
        if (drugId == null || quality == null) {
            plugin.getLogger().warning("Invalid seed data at " + location);
            return;
        }
        crops.put(location, new Crop(drugId, quality, System.currentTimeMillis()));
        saveCrops();
        plugin.getLogger().info("Planted crop at " + location + ": " + drugId + " (" + quality + ")");
    }

    public void removeCrop(Location location) {
        crops.remove(location);
        saveCrops();
        plugin.getLogger().info("Removed crop at " + location);
    }

    public Crop getCrop(Location location) {
        return crops.get(location);
    }

    public double getCurrentGrowth(Crop crop) {
        long timeSincePlanted = System.currentTimeMillis() - crop.getPlantedTime();
        return Math.min((double) timeSincePlanted / (600 * 1000) * 100, 100.0);
    }

    public boolean isDrugCrop(Block block) {
        return crops.containsKey(block.getLocation()) && block.getType() == Material.WHEAT;
    }

    public static class Crop {
        private final String drugId;
        private final String quality;
        private final long plantedTime;

        public Crop(String drugId, String quality, long plantedTime) {
            this.drugId = drugId;
            this.quality = quality;
            this.plantedTime = plantedTime;
        }

        public String getDrugId() {
            return drugId;
        }

        public String getQuality() {
            return quality;
        }

        public long getPlantedTime() {
            return plantedTime;
        }
    }
}