package com.spence.drugcraft.crops;

import org.bukkit.Location;

public class Crop {
    private final String drugId;
    private final Location location;
    private long plantedTime;
    private double growthProgress;
    private Object hologram; // Placeholder for DecentHolograms integration

    public Crop(String drugId, Location location) {
        this.drugId = drugId;
        this.location = location;
        this.plantedTime = System.currentTimeMillis();
        this.growthProgress = 0.0;
    }

    public String getDrugId() {
        return drugId;
    }

    public Location getLocation() {
        return location;
    }

    public double getGrowthProgress() {
        return growthProgress;
    }

    public void updateGrowth(double increment) {
        this.growthProgress = Math.min(growthProgress + increment, 100.0);
    }

    public boolean isHarvestable() {
        return growthProgress >= 100.0;
    }

    public void removeHologram() {
        // Placeholder: Remove hologram using DecentHolograms API
        if (hologram != null) {
            hologram = null;
        }
    }
}