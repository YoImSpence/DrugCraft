package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CropManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final DataManager dataManager;
    private final Map<String, Crop> crops = new HashMap<>();
    private final Map<String, Item> cropIcons = new HashMap<>();
    private final Map<String, String> hologramIds = new HashMap<>(); // Track hologram IDs by location key
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
        createHologram(crop);
        updateCropAge(crop);
        logger.info("Added crop: " + crop.getDrugId() + " at " + key);
    }

    private void createHologram(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        String hologramId = "crop_" + key;
        Location hologramLoc = crop.getLocation().clone().add(0.5, 1.5, 0.5);
        hologramLoc.setPitch(0);
        hologramLoc.setYaw(0);

        // Check for existing hologram at this location
        if (hologramIds.containsKey(key)) {
            Hologram existingHologram = DHAPI.getHologram(hologramIds.get(key));
            if (existingHologram != null) {
                logger.fine("Found existing hologram at " + hologramLoc + ", updating instead of creating new");
                updateHologramLines(existingHologram, crop);
                crop.setHologramId(existingHologram.getId());
                return;
            } else {
                hologramIds.remove(key); // Clean up stale ID
            }
        }

        // Remove any old hologram with same ID
        Hologram existingHologram = DHAPI.getHologram(hologramId);
        if (existingHologram != null) {
            existingHologram.delete();
            logger.fine("Removed existing hologram for crop " + crop.getDrugId() + " at " + key);
        }

        Hologram hologram = DHAPI.createHologram(hologramId, hologramLoc);
        if (hologram == null) {
            logger.severe("Failed to create hologram for crop " + crop.getDrugId() + " at " + hologramLoc);
            return;
        }
        Drug drug = drugManager.getDrug(crop.getDrugId());
        String drugName = drug != null ? drug.getName() : crop.getDrugId();
        if (drug == null) {
            logger.warning("Drug not found for crop ID: " + crop.getDrugId() + " at " + key);
        }
        double growth = getGrowthPercentage(crop);
        String status = growth >= 100 ? "{#AA00AA}Mature" : "{#FF55FF}Growing";
        List<String> hologramLines = Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "{#00FFFF}" + drugName),
                ChatColor.translateAlternateColorCodes('&', "{#55FF55}Growth: {#FFFFFF}" + String.format("%.2f", growth) + "%"),
                ChatColor.translateAlternateColorCodes('&', status)
        );
        logger.fine("Creating hologram for crop " + crop.getDrugId() + " at " + hologramLoc + " with lines: " + hologramLines);
        DHAPI.setHologramLines(hologram, hologramLines);
        crop.setHologramId(hologramId);
        hologramIds.put(key, hologramId);

        // Spawn floating item icon
        if (drug != null) {
            ItemStack item = drug.getItem();
            Location iconLoc = crop.getLocation().clone().add(0.5, 0.8, 0.5); // Adjusted to Y+0.8
            iconLoc.setPitch(0);
            iconLoc.setYaw(0);
            Item icon = crop.getLocation().getWorld().dropItem(iconLoc, item);
            icon.setPickupDelay(Integer.MAX_VALUE);
            icon.setInvulnerable(true);
            icon.setGravity(false);
            icon.setVelocity(new Vector(0, 0, 0));
            icon.setCustomNameVisible(false);
            icon.setTicksLived(1);
            cropIcons.put(key, icon);
            // Lock item position with repeating task
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!crops.containsKey(key) || !icon.isValid()) {
                        icon.remove();
                        cropIcons.remove(key);
                        cancel();
                        logger.fine("Removed item icon for crop " + crop.getDrugId() + " at " + key + " (crop removed or invalid)");
                        return;
                    }
                    Location currentLoc = icon.getLocation();
                    if (!currentLoc.equals(iconLoc)) {
                        logger.fine("Correcting item icon position for crop " + crop.getDrugId() + " from " + currentLoc + " to " + iconLoc);
                        icon.teleport(iconLoc);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
            logger.fine("Spawned and locked item icon for crop " + crop.getDrugId() + " at " + iconLoc);
        }
    }

    private void updateHologramLines(Hologram hologram, Crop crop) {
        Drug drug = drugManager.getDrug(crop.getDrugId());
        String drugName = drug != null ? drug.getName() : crop.getDrugId();
        double growth = getGrowthPercentage(crop);
        String status = growth >= 100 ? "{#AA00AA}Mature" : "{#FF55FF}Growing";
        List<String> hologramLines = Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "{#00FFFF}" + drugName),
                ChatColor.translateAlternateColorCodes('&', "{#55FF55}Growth: {#FFFFFF}" + String.format("%.2f", growth) + "%"),
                ChatColor.translateAlternateColorCodes('&', status)
        );
        DHAPI.setHologramLines(hologram, hologramLines);
        logger.fine("Updated hologram lines for crop " + crop.getDrugId() + " at " + hologram.getLocation() + ": " + hologramLines);
    }

    public Crop getCrop(Location location) {
        Crop crop = crops.get(getLocationKey(location));
        if (crop == null) {
            logger.fine("No crop found at location: " + getLocationKey(location));
        }
        return crop;
    }

    public void removeCrop(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        crops.remove(key);
        dataManager.removeCrop(crop);
        if (crop.getHologramId() != null) {
            Hologram hologram = DHAPI.getHologram(crop.getHologramId());
            if (hologram != null) {
                hologram.delete();
                logger.fine("Deleted hologram for crop " + crop.getDrugId() + " at " + key);
            }
            hologramIds.remove(key);
        }
        Item icon = cropIcons.remove(key);
        if (icon != null) {
            icon.remove();
            logger.fine("Removed item icon for crop " + crop.getDrugId() + " at " + key);
        }
        logger.info("Removed crop: " + crop.getDrugId() + " at " + key);
    }

    public double getGrowthPercentage(Crop crop) {
        Drug drug = drugManager.getDrug(crop.getDrugId());
        if (drug == null) {
            logger.warning("Drug not found for crop ID: " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()));
            return 0;
        }
        long timeElapsed = (System.currentTimeMillis() - crop.getPlantingTime()) / 1000;
        double growthTime = drug.getGrowthTime();
        // Apply wet farmland boost
        Block block = crop.getLocation().getBlock().getRelative(0, -1, 0);
        if (block.getType() == Material.FARMLAND) {
            Farmland farmland = (Farmland) block.getBlockData();
            if (farmland.getMoisture() == farmland.getMaximumMoisture()) {
                double wetMultiplier = plugin.getConfigManager().getWetFarmlandMultiplier();
                growthTime *= wetMultiplier;
            }
        }
        double percentage = (double) timeElapsed / growthTime * 100;
        logger.fine("Growth percentage for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + ": " + percentage + "%");
        return Math.min(percentage, 100);
    }

    public void updateCropAge(Crop crop) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = crop.getLocation().getBlock();
                // Clear 5x5 area around crop (Y+1 and Y+2), excluding target crop
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x != 0 || z != 0) { // Skip target crop position
                            Block relative = block.getRelative(x, 1, z);
                            if (relative.getType() == Material.WHEAT) {
                                logger.fine("Clearing duplicate wheat block at " + relative.getLocation());
                                relative.setType(Material.AIR);
                            }
                        }
                    }
                }
                // Initialize block state
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getType() != Material.WHEAT) {
                            logger.warning("Crop block at " + getLocationKey(crop.getLocation()) + " is not wheat, setting to wheat");
                            block.setType(Material.AIR);
                            block.setType(Material.WHEAT);
                        }
                        double growth = getGrowthPercentage(crop);
                        int age = (int) (growth / 100 * 7); // Map 0-100% to age 0-7
                        crop.setAge(age);
                        try {
                            block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                            Ageable ageable = (Ageable) block.getBlockData();
                            ageable.setAge(age);
                            block.setBlockData(ageable);
                            block.getState().update(true, true); // Force client update
                            logger.info("Updated crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + " to age " + age + " (growth: " + String.format("%.2f", growth) + "%)");
                        } catch (ClassCastException e) {
                            logger.severe("Failed to set age for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + ": " + e.getMessage());
                            block.setType(Material.AIR);
                            block.setType(Material.WHEAT);
                            block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                            block.getState().update(true, true);
                        }
                    }
                }.runTaskLater(plugin, 1L); // Delay to ensure block sync
            }
        }.runTask(plugin);
    }

    private void startUpdateTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Crop crop : crops.values()) {
                double growth = getGrowthPercentage(crop);
                updateCropAge(crop);
                Hologram hologram = DHAPI.getHologram(crop.getHologramId());
                if (hologram == null) {
                    logger.warning("Hologram not found for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + ", recreating");
                    createHologram(crop);
                    hologram = DHAPI.getHologram(crop.getHologramId());
                }
                if (hologram != null) {
                    updateHologramLines(hologram, crop);
                } else {
                    logger.severe("Failed to recreate hologram for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()));
                }
            }
        }, 0L, 20L * plugin.getConfigManager().getConfig().getInt("hologram.update_interval", 5));
    }

    public String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    public Map<String, Crop> getCrops() {
        return crops;
    }
}