package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;

public class Crop {
    private final DrugCraft plugin;
    private final Location location;
    private final String strain;
    private final String quality;
    private Hologram hologram;
    private boolean harvestable;
    private long growthTime;

    public Crop(DrugCraft plugin, Location location, String strain, String quality, long growthTime) {
        this.plugin = plugin;
        this.location = location;
        this.strain = strain;
        this.quality = quality;
        this.growthTime = growthTime;
        this.harvestable = false;
        updateHologram();
    }

    public void update() {
        if (growthTime > 0) {
            growthTime--;
            updateHologram();
            if (growthTime <= 0) {
                harvestable = true;
                updateHologram();
            }
        }
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.destroy();
            hologram = null;
        }
    }

    private void updateHologram() {
        removeHologram();
        Block block = location.getBlock();
        if (block.getType() == Material.WHEAT) {
            String text = harvestable ? MessageUtils.getMessage("crops.harvestable", "strain", strain, "quality", quality) :
                    MessageUtils.getMessage("crops.growing", "strain", strain, "quality", quality, "time", String.valueOf(growthTime / 20));
            hologram = DHAPI.createHologram("crop_" + location.toString(), location.clone().add(0.5, 1.0, 0.5), Arrays.asList(text));
        }
    }

    public Location getLocation() { return location; }
    public String getStrain() { return strain; }
    public String getQuality() { return quality; }
    public boolean isHarvestable() { return harvestable; }
}