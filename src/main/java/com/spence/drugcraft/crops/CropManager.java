package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CropManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final DataManager dataManager;
    private final GrowLight growLight;
    private final Map<String, Crop> crops = new HashMap<>();
    private final Map<String, Item> cropIcons = new HashMap<>();
    private final Map<String, String> hologramIds = new HashMap<>();
    private final Logger logger;

    public CropManager(DrugCraft plugin, DrugManager drugManager, DataManager dataManager, GrowLight growLight) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.dataManager = dataManager;
        this.growLight = growLight;
        this.logger = plugin.getLogger();
        startUpdateTask();
    }

    public void addCrop(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        Player player = Bukkit.getPlayer(crop.getPlayerUUID());
        if (player == null) {
            logger.warning("Player not found for crop " + crop.getDrugId() + " at " + key);
            return;
        }
        if (!plugin.getHouseManager().canUseDrugBlocks(player, crop.getLocation())) {
            player.sendMessage(MessageUtils.color("&#FF4040You can only plant drug crops in a house you own."));
            return;
        }
        crops.put(key, crop);
        dataManager.saveCrop(crop);
        createHologram(crop);
        updateCropAge(crop);
        logger.info("Added crop: " + crop.getDrugId() + " at " + key);
    }

    private void createHologram(Crop crop) {
        String key = getLocationKey(crop.getLocation());
        String hologramId = "crop_" + key;
        Location hologramLoc = crop.getLocation().clone().add(0.5, 2.5, 0.5);
        boolean hasGrowLight = false;
        String growLightQuality = "Basic";
        for (int y = 1; y <= 3; y++) {
            Location aboveLoc = crop.getLocation().clone().add(0, y, 0);
            String quality = growLight.getQualityAtLocation(aboveLoc);
            if (quality != null) {
                hasGrowLight = true;
                growLightQuality = quality;
                if (y == 1) {
                    hologramLoc = crop.getLocation().clone().add(0.5, 1.5, 0.5);
                }
                break;
            }
        }
        hologramLoc.setPitch(0);
        hologramLoc.setYaw(0);

        if (hologramIds.containsKey(key)) {
            Hologram existingHologram = DHAPI.getHologram(hologramIds.get(key));
            if (existingHologram != null) {
                existingHologram.delete();
                logger.fine("Removed existing hologram at " + hologramLoc + " for crop " + crop.getDrugId());
            }
            hologramIds.remove(key);
        }

        Hologram hologram = DHAPI.createHologram(hologramId, hologramLoc);
        if (hologram == null) {
            logger.severe("Failed to create hologram for crop " + crop.getDrugId() + " at " + hologramLoc);
            return;
        }
        updateHologramLines(hologram, crop, hasGrowLight, growLightQuality);
        crop.setHologramId(hologramId);
        hologramIds.put(key, hologramId);

        Drug drug = drugManager.getDrug(crop.getDrugId());
        String drugName = drug != null ? drug.getName() : crop.getDrugId();
        if (drug == null) {
            logger.warning("Drug not found for crop ID: " + crop.getDrugId() + " at " + key);
            return;
        }
        ItemStack item = drugManager.getDrugItem(crop.getDrugId(), crop.getQuality());
        if (item == null) {
            logger.warning("Failed to create item icon for crop " + crop.getDrugId() + " at " + key);
            return;
        }
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
                    logger.fine("Removed item icon for crop " + crop.getDrugId() + " at " + key);
                    return;
                }
                Location currentLoc = icon.getLocation();
                if (!currentLoc.equals(iconLoc)) {
                    icon.teleport(iconLoc);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        logger.fine("Created hologram and icon for crop " + drugName + " at " + key);
    }

    private void updateHologramLines(Hologram hologram, Crop crop, boolean hasGrowLight, String growLightQuality) {
        Drug drug = drugManager.getDrug(crop.getDrugId());
        String drugName = drug != null ? drug.getName() : crop.getDrugId();
        double growth = getGrowthPercentage(crop, growLightQuality);
        String status = growth >= 100 ? "&#FFFF00Harvestable" : "&#FF7F00Growing: &#D3D3D3" + String.format("%.2f", growth) + "%";
        List<String> hologramLines = new ArrayList<>(Arrays.asList(
                "",
                MessageUtils.color("&#FFFF00&l" + drugName),
                MessageUtils.color(status)
        ));
        if (hasGrowLight) {
            hologramLines.add(MessageUtils.color("&#FFDAB9Light Boosted"));
        }
        DHAPI.setHologramLines(hologram, hologramLines);
        logger.fine("Updated hologram for crop " + drugName + " at " + hologram.getLocation() + ": " + hologramLines);
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
                player.sendMessage(MessageUtils.color("&#FF7F00Cleared " + cleared + " drug crops and their data."));
                logger.info("Player " + player.getName() + " cleared " + cleared + " drug crops");
            }
        }.runTask(plugin);
    }

    public double getGrowthPercentage(Crop crop, String growLightQuality) {
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
                logger.fine("Applied wet farmland multiplier " + wetMultiplier + " for crop " + crop.getDrugId());
            }
        }
        double multiplier = 1.0;
        boolean hasGrowLight = false;
        for (int y = 1; y <= 3; y++) {
            Location aboveLoc = crop.getLocation().clone().add(0, y, 0);
            String quality = growLight.getQualityAtLocation(aboveLoc);
            if (quality != null) {
                hasGrowLight = true;
                growLightQuality = quality;
                multiplier = switch (growLightQuality) {
                    case "Legendary" -> 0.5;
                    case "Prime" -> 0.6;
                    case "Exotic" -> 0.7;
                    case "Standard" -> 0.8;
                    default -> 0.9; // Basic
                };
                Location particleLoc = crop.getLocation().clone().add(0.5, 0.5, 0.5);
                crop.getLocation().getWorld().spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 5, 0.3, 0.3, 0.3, 0.1);
                logger.fine("Applied grow light boost (quality: " + growLightQuality + ", multiplier: " + multiplier + ") at " + aboveLoc);
                break;
            }
        }
        growthTime *= multiplier;
        double cartelBonus = plugin.getCartelManager().getGrowthBonus(crop.getLocation());
        growthTime *= (1 - cartelBonus);
        double percentage = Math.min((double) timeElapsed / growthTime * 100, 100);
        logger.fine("Growth percentage for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()) + ": " + percentage + "%");
        return percentage;
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
                                relative.setType(Material.AIR);
                                logger.fine("Cleared duplicate wheat block at " + relative.getLocation());
                            }
                        }
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getType() != Material.WHEAT) {
                            block.setType(Material.WHEAT);
                            logger.fine("Restored wheat block for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()));
                        }
                        String growLightQuality = "Basic";
                        boolean hasGrowLight = false;
                        for (int y = 1; y <= 3; y++) {
                            Location aboveLoc = crop.getLocation().clone().add(0, y, 0);
                            String quality = growLight.getQualityAtLocation(aboveLoc);
                            if (quality != null) {
                                hasGrowLight = true;
                                growLightQuality = quality;
                                break;
                            }
                        }
                        double growth = getGrowthPercentage(crop, growLightQuality);
                        int age = (int) (growth / 100 * 7);
                        crop.setAge(age);
                        try {
                            Ageable ageable = (Ageable) block.getBlockData();
                            ageable.setAge(age);
                            block.setBlockData(ageable);
                            block.getState().update(true, true);
                            logger.fine("Updated crop " + crop.getDrugId() + " age to " + age + " (growth: " + growth + "%)");
                        } catch (ClassCastException e) {
                            logger.severe("Failed to set age for crop " + crop.getDrugId() + ": " + e.getMessage());
                            block.setType(Material.AIR);
                            block.setType(Material.WHEAT);
                        }
                        Hologram hologram = DHAPI.getHologram(crop.getHologramId());
                        if (hologram != null) {
                            updateHologramLines(hologram, crop, hasGrowLight, growLightQuality);
                        } else {
                            createHologram(crop);
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
        }.runTask(plugin);
    }

    private void startUpdateTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Crop crop : crops.values()) {
                String growLightQuality = "Basic";
                boolean hasGrowLight = false;
                for (int y = 1; y <= 3; y++) {
                    Location aboveLoc = crop.getLocation().clone().add(0, y, 0);
                    String quality = growLight.getQualityAtLocation(aboveLoc);
                    if (quality != null) {
                        hasGrowLight = true;
                        growLightQuality = quality;
                        break;
                    }
                }
                double growth = getGrowthPercentage(crop, growLightQuality);
                updateCropAge(crop);
                Hologram hologram = DHAPI.getHologram(crop.getHologramId());
                if (hologram == null) {
                    createHologram(crop);
                    logger.fine("Recreated hologram for crop " + crop.getDrugId() + " at " + getLocationKey(crop.getLocation()));
                } else {
                    updateHologramLines(hologram, crop, hasGrowLight, growLightQuality);
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

    public int getYieldMultiplier(String quality) {
        return switch (quality) {
            case "Legendary" -> 5;
            case "Prime" -> 4;
            case "Exotic" -> 3;
            case "Standard" -> 2;
            default -> 1; // Basic
        };
    }
}