package com.spence.drugcraft.crops;

import org.bukkit.Location;

public class Crop {
    private final Location location;
    private final String drugId;
    private final long plantingTime;
    private String hologramId;

    public Crop(Location location, String drugId, long plantingTime) {
        this.location = location;
        this.drugId = drugId;
        this.plantingTime = plantingTime;
    }

    public Location getLocation() {
        return location;
    }

    public String getDrugId() {
        return drugId;
    }

    public long getPlantingTime() {
        return plantingTime;
    }

    public String getHologramId() {
        return hologramId;
    }

    public void setHologramId(String hologramId) {
        this.hologramId = hologramId;
    }
}