package com.spence.drugcraft.crops;

import org.bukkit.Location;

public class Crop {
    private final Location location;
    private final String drugId;
    private long plantingTime;
    private int age;
    private String hologramId;

    public Crop(Location location, String drugId, long plantingTime, int age, String hologramId) {
        this.location = location;
        this.drugId = drugId;
        this.plantingTime = plantingTime;
        this.age = age;
        this.hologramId = hologramId;
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

    public void setPlantingTime(long plantingTime) {
        this.plantingTime = plantingTime;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getHologramId() {
        return hologramId;
    }

    public void setHologramId(String hologramId) {
        this.hologramId = hologramId;
    }
}