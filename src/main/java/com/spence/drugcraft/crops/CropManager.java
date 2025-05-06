package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CropManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final DataManager dataManager;
    private final Map<String, Crop> crops = new HashMap<>();
    private final Map<String, Item> cropIcons = new HashMap<>();
    private final Map<String, String> hologramIds = new HashMap<>();
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
        Location hologramLoc = crop.getLocation().clone().add(0.5, 2.0, 0.5);
        hologramLoc.setPitch(0);
        hologramLoc.setYaw(0);

        if (hologramIds.containsKey(key)) {
            Hologram existingHologram = DHAPI.getHologram(hologramIds.get(key));
            if (existingHologram != null) {
                logger.fine("Found existing hologram at " + hologramLoc + ", updating instead of creating new");
                updateHologramLines(existingHologram, crop);
                crop.setHologramId(existingHologram.getId());
                return;
            } else {
                hologramIds.remove(key);
            }
        }

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
        String status = growth >= 100 ? "{#FF00FF}Harvestable" : "{#00FF00}Growing: {#FFFFFF}" + String.format("%.2f", growth) + "%";
        List<String> hologramLines = Arrays.asList(
                "",
                MessageUtils.color("{#FFD700}" + drugName),
                MessageUtils.color(status)
        );
        logger.fine("Creating hologram for crop " + crop.getDrugId() + " at " + hologramLoc + " with lines: " + hologramLines);
        DHAPI.setHologramLines(hologram, hologramLines);
        crop.setHologramId(hologramId);
        hologramIds.put(key, hologramId);

        if (drug != null) {
            ItemStack item = drug.getItem(null);
            Location iconLoc = crop.getLocation().clone().add(0.5, 1.7, 0.5);
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
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!crops.containsKey(key) || !icon.isValid() || crop.getLocation().getBlock().getType() != Material.WHEAT) {
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
        String status = growth >= 100 ? "{#FF00FF}Harvestable" : "{#00FF00}Growing: {#FFFFFF}" + String.format("%.2f", growth) + "%";
        List<String> hologramLines = Arrays.asList(
                "",
                MessageUtils.color("{#FFD700}" + drugName),
                MessageUtils.color(status)
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

    public void cleanupHolograms() {
        for (String key : hologramIds.keySet()) {
            Hologram hologram = DHAPI.getHologram(hologramIds.get(key));
            if (hologram != null) {
                hologram.delete();
                logger.fine("Cleaned up hologram at " + key);
            }
        }
        for (Item icon : cropIcons.values()) {
            if (icon != null && icon.isValid()) {
                icon.remove();
                logger.fine("Cleaned up item icon at " + icon.getLocation());
            }
        }
        hologramIds.clear();
        cropIcons.clear();
        logger.info("Cleaned up all holograms and item icons");
    }

    public void clearAllCrops(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int cleared = 0;
                List<Crop> cropsCopy = new ArrayList<>(crops.values());
                for (Crop crop : cropsCopy) {
                    Block block = crop.getLocation().getBlock();
                    block.setType(Material.AIR);
                    removeCrop(crop);
                    cleared++;
                }
                dataManager.saveCrops();
                player.sendMessage(MessageUtils.color("{#00FF00}Cleared " + cleared + " drug crops and their data."));
                logger.info("Player " + player.getName() + " cleared " + cleared + " drug crops");
            }
        }.runTask(plugin);
    }

    public double getGrowthPercentage(Crop crop) {
        Drug drug = drugManager.getDrug(crop.getDrugId());
        if (drug == null) {
            logger.warning("Drug not found for crop ID: " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()));
            return 0;
        }
        long timeElapsed = (System.currentTimeMillis() - crop.getPlantingTime()) / 1000;
        double growthTime = drug.getGrowthTime();
        Block block = crop.getLocation().getBlock().getRelative(0, -1, 0);
        if (block.getType() == Material.FARMLAND) {
            Farmland farmland = (Farmland) block.getBlockData();
            if (farmland.getMoisture() == farmland.getMaximumMoisture()) {
                double wetMultiplier = plugin.getConfigManager().getWetFarmlandMultiplier();
                growthTime *= wetMultiplier;
            }
        }
        // Check for grow lights above (up to 3 blocks)
        int growLightCount = 0;
        for (int y = 1; y <= 3; y++) {
            Block above = block.getRelative(0, y + 1, 0);
            if (above.getType() == Material.REDSTONE_LAMP && above.isBlockPowered()) {
                growLightCount++;
                logger.fine("Applied grow light boost at " + above.getLocation() + " for crop " + crop.getDrugId());
            }
        }
        if (growLightCount > 0) {
            growthTime *= Math.pow(0.7, growLightCount); // 0.7x per grow light, stackable
        }
        double cartelBonus = plugin.getCartelManager().getGrowthBonus(crop.getLocation());
        growthTime *= (1 - cartelBonus);
        double percentage = (double) timeElapsed / growthTime * 100;
        logger.fine("Growth percentage for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + ": " + percentage + "%");
        return Math.min(percentage, 100);
    }

    public void updateCropAge(Crop crop) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = crop.getLocation().getBlock();
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x != 0 || z != 0) {
                            Block relative = block.getRelative(x, 1, z);
                            if (relative.getType() == Material.WHEAT) {
                                logger.fine("Clearing duplicate wheat block at " + relative.getLocation());
                                relative.setType(Material.AIR);
                            }
                        }
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getType() != Material.WHEAT) {
                            logger.warning("Crop block at " + getLocationKey(crop.getLocation()) + " is not wheat, setting to wheat");
                            block.setType(Material.AIR);
                            block.setType(Material.WHEAT);
                        }
                        double growth = getGrowthPercentage(crop);
                        int age = (int) (growth / 100 * 7);
                        crop.setAge(age);
                        try {
                            block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                            Ageable ageable = (Ageable) block.getBlockData();
                            ageable.setAge(age);
                            block.setBlockData(ageable);
                            block.getState().update(true, true);
                            logger.info("Updated crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + " to age " + age + " (growth: " + String.format("%.2f", growth) + "%)");
                        } catch (ClassCastException e) {
                            logger.severe("Failed to set age for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + ": " + e.getMessage());
                            block.setType(Material.AIR);
                            block.setType(Material.WHEAT);
                            block.setBlockData(Bukkit.createBlockData(Material.WHEAT), true);
                            block.getState().update(true, true);
                        }
                    }
                }.runTaskLater(plugin, 1L);
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