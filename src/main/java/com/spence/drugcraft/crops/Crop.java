package com.spence.drugcraft.crops;

import org.bukkit.Location;

public class Crop {
    private final Location location;
    private final String drugId;
    private final long plantingTime;
    private String hologramId;
    private int age; // Wheat age (0-7)

    public Crop(Location location, String drugId, long plantingTime) {
        this.location = location;
        this.drugId = drugId;
        this.plantingTime = plantingTime;
        this.age = 0;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = Math.max(0, Math.min(7, age));
    }
}