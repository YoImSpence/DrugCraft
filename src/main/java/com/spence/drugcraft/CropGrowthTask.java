package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages growth and persistence of DrugCraft crops, including holograms.
 */
public class CropGrowthTask extends BukkitRunnable {
    private final DrugCraft plugin;
    private final File cropsFile;
    private final FileConfiguration cropsConfig;
    private final Map<Location, CropData> crops;
    private final Map<String, Map<Integer, GrowthTimer>> growthTimers;
    private final Map<String, List<String>> hologramFormats;
    private boolean needsSave;

    public CropGrowthTask(DrugCraft plugin) {
        this.plugin = plugin;
        this.cropsFile = new File(plugin.getDataFolder(), "crops.yml");
        this.cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
        this.crops = new HashMap<>();
        this.growthTimers = new HashMap<>();
        this.hologramFormats = new HashMap<>();
        this.needsSave = false;
        loadCrops();
        loadCropConfigs();
    }

    /**
     * Loads crop data from crops.yml.
     */
    private void loadCrops() {
        ConfigurationSection cropsSection = cropsConfig.getConfigurationSection("crops");
        if (cropsSection == null) {
            return;
        }
        for (String key : cropsSection.getKeys(false)) {
            ConfigurationSection crop = cropsSection.getConfigurationSection(key);
            if (crop == null) continue;
            Location location = (Location) crop.get("location");
            String strain = crop.getString("strain");
            int stage = crop.getInt("stage");
            long timePlanted = crop.getLong("time_planted");
            if (location != null && strain != null) {
                crops.put(location, new CropData(location, strain, stage, timePlanted));
            }
        }
        plugin.getLogger().info("Loaded " + crops.size() + " crops from crops.yml");
    }

    /**
     * Loads growth timers and hologram formats from config.yml.
     */
    private void loadCropConfigs() {
        ConfigurationSection cropsSection = plugin.getConfig().getConfigurationSection("crops");
        if (cropsSection == null) {
            setDefaultConfigs();
            return;
        }
        for (String strain : cropsSection.getKeys(false)) {
            ConfigurationSection strainSection = cropsSection.getConfigurationSection(strain + ".growth_timers");
            Map<Integer, GrowthTimer> timers = new HashMap<>();
            if (strainSection != null) {
                for (String stageKey : strainSection.getKeys(false)) {
                    int stage = Integer.parseInt(stageKey.replace("stage_", ""));
                    int minTicks = strainSection.getInt(stageKey + ".min_ticks", 600);
                    int maxTicks = strainSection.getInt(stageKey + ".max_ticks", 1200);
                    timers.put(stage, new GrowthTimer(minTicks, maxTicks));
                }
            }
            growthTimers.put(strain.toLowerCase(), timers);

            List<String> hologramLines = cropsSection.getStringList(strain + ".hologram_format");
            if (hologramLines.isEmpty()) {
                hologramLines = Arrays.asList("§a{strain} Crop", "§7Stage: {stage}");
            }
            hologramFormats.put(strain.toLowerCase(), hologramLines);
        }
        plugin.getLogger().info("Loaded crop configurations for " + growthTimers.size() + " strains");
    }

    /**
     * Sets default growth timers and hologram formats for cannabis strains.
     */
    private void setDefaultConfigs() {
        String[] strains = {"cannabis_sativa", "cannabis_indica", "cannabis_hybrid"};
        for (String strain : strains) {
            Map<Integer, GrowthTimer> timers = new HashMap<>();
            for (int stage = 1; stage <= 3; stage++) {
                timers.put(stage, new GrowthTimer(600, 1200));
            }
            growthTimers.put(strain, timers);
            hologramFormats.put(strain, Arrays.asList("§a{strain} Crop", "§7Stage: {stage}"));
        }
    }

    @Override
    public void run() {
        List<Location> toRemove = new ArrayList<>();
        for (Map.Entry<Location, CropData> entry : crops.entrySet()) {
            Location location = entry.getKey();
            CropData crop = entry.getValue();
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - crop.timePlanted;
            Map<Integer, GrowthTimer> timers = growthTimers.getOrDefault(crop.strain.toLowerCase(), growthTimers.get("cannabis_sativa"));
            GrowthTimer timer = timers.get(crop.stage);
            if (timer == null) {
                timer = new GrowthTimer(600, 1200);
            }
            long ticksElapsed = elapsed / 50;
            if (ticksElapsed >= ThreadLocalRandom.current().nextInt(timer.minTicks, timer.maxTicks + 1)) {
                crop.stage++;
                crop.timePlanted = currentTime;
                if (crop.stage > 3) {
                    toRemove.add(location);
                    removeHologram(location);
                    continue;
                }
                updateCrop(location, crop);
                updateHologram(location, crop);
                needsSave = true;
            }
        }
        toRemove.forEach(crops::remove);
        if (needsSave) {
            saveCrops();
            needsSave = false;
        }
    }

    /**
     * Adds a new crop at the specified location with the given strain.
     */
    public void addCrop(Location location, String strain) {
        Block farmland = location.getBlock().getRelative(0, -1, 0);
        if (farmland.getType() != Material.FARMLAND) {
            plugin.getLogger().warning("Failed to add crop at " + location + ": Not on farmland");
            return;
        }
        CropData crop = new CropData(location.clone(), strain, 0, System.currentTimeMillis());
        crops.put(location.clone(), crop);
        updateCrop(location, crop);
        updateHologram(location, crop);
        needsSave = true;
    }

    /**
     * Updates the crop block’s appearance based on its stage.
     */
    private void updateCrop(Location location, CropData crop) {
        Block block = location.getBlock();
        Block farmland = block.getRelative(0, -1, 0);
        if (farmland.getType() != Material.FARMLAND) {
            crops.remove(location);
            removeHologram(location);
            needsSave = true;
            return;
        }
        block.setType(Material.WHEAT);
        Ageable ageable = (Ageable) block.getBlockData();
        ageable.setAge(Math.min(crop.stage, ageable.getMaximumAge()));
        block.setBlockData(ageable);
    }

    /**
     * Updates or creates the hologram for a crop.
     */
    private void updateHologram(Location location, CropData crop) {
        String hologramName = String.format("crop_%d_%d_%d_%s",
                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                location.getWorld().getName().replaceAll("[^a-zA-Z0-9_-]", "_"));
        Hologram hologram = DHAPI.getHologram(hologramName);
        Location hologramLoc = location.clone().add(0.5, 1.5, 0.5);
        List<String> lines = hologramFormats.getOrDefault(crop.strain.toLowerCase(),
                Arrays.asList("§a{strain} Crop", "§7Stage: {stage}"));
        List<String> formattedLines = new ArrayList<>();
        for (String line : lines) {
            formattedLines.add(line.replace("{strain}", crop.strain).replace("{stage}", String.valueOf(crop.stage)));
        }
        try {
            if (hologram == null) {
                hologram = DHAPI.createHologram(hologramName, hologramLoc, formattedLines);
            } else {
                DHAPI.moveHologram(hologram, hologramLoc);
                DHAPI.setHologramLines(hologram, formattedLines);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create/update hologram at " + location + ": " + e.getMessage());
        }
    }

    /**
     * Removes the hologram for a crop.
     */
    private void removeHologram(Location location) {
        String hologramName = String.format("crop_%d_%d_%d_%s",
                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                location.getWorld().getName().replaceAll("[^a-zA-Z0-9_-]", "_"));
        Hologram hologram = DHAPI.getHologram(hologramName);
        if (hologram != null) {
            DHAPI.removeHologram(hologramName);
            plugin.getLogger().info("Removed hologram at " + location);
        }
    }

    /**
     * Saves crop data to crops.yml.
     */
    private void saveCrops() {
        if (crops.isEmpty()) {
            return;
        }
        cropsConfig.set("crops", null);
        int index = 1;
        for (Map.Entry<Location, CropData> entry : crops.entrySet()) {
            Location location = entry.getKey();
            CropData crop = entry.getValue();
            String path = "crops." + crop.strain.toLowerCase() + "_" + index;
            cropsConfig.set(path + ".location", location);
            cropsConfig.set(path + ".strain", crop.strain);
            cropsConfig.set(path + ".stage", crop.stage);
            cropsConfig.set(path + ".time_planted", crop.timePlanted);
            index++;
        }
        try {
            cropsConfig.save(cropsFile);
            plugin.getLogger().info("Saved " + crops.size() + " crops to crops.yml");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save crops.yml: " + e.getMessage());
        }
    }

    /**
     * Retrieves crop data for a location.
     * @param location The crop’s location.
     * @return The CropData, or null if not found.
     */
    public CropData getCrop(Location location) {
        return crops.entrySet().stream()
                .filter(entry -> entry.getKey().getBlockX() == location.getBlockX() &&
                        entry.getKey().getBlockY() == location.getBlockY() &&
                        entry.getKey().getBlockZ() == location.getBlockZ() &&
                        entry.getKey().getWorld().equals(location.getWorld()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes a crop and its hologram.
     * @param location The crop’s location.
     */
    public void removeCrop(Location location) {
        Location key = crops.keySet().stream()
                .filter(loc -> loc.getBlockX() == location.getBlockX() &&
                        loc.getBlockY() == location.getBlockY() &&
                        loc.getBlockZ() == location.getBlockZ() &&
                        loc.getWorld().equals(location.getWorld()))
                .findFirst()
                .orElse(null);
        if (key != null) {
            crops.remove(key);
            removeHologram(location);
            needsSave = true;
            plugin.getLogger().info("Removed crop at " + location);
        }
    }

    /**
     * Retrieves the drug item for a strain.
     * @param strain The crop’s strain (e.g., "cannabis_sativa").
     * @return The drug item, or null if invalid.
     */
    public ItemStack getDrugItem(String strain) {
        Drug drug = plugin.getDrugManager().getDrug(strain.toLowerCase());
        return drug != null ? drug.getItem() : null;
    }

    public static class CropData {
        Location location;
        String strain;
        int stage;
        long timePlanted;

        CropData(Location location, String strain, int stage, long timePlanted) {
            this.location = location;
            this.strain = strain;
            this.stage = stage;
            this.timePlanted = timePlanted;
        }
    }

    public static class GrowthTimer {
        int minTicks;
        int maxTicks;

        GrowthTimer(int minTicks, int maxTicks) {
            this.minTicks = minTicks;
            this.maxTicks = maxTicks;
        }
    }
}