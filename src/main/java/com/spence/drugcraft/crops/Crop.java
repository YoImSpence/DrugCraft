package com.spence.drugcraft.crops;

import org.bukkit.Location;

import java.util.UUID;

public class Crop {
    private final String drugId;
    private final Location location;
    private final UUID playerUUID;
    private final long plantingTime;
    private final String quality;
    private String hologramId;
    private int age;

    public Crop(String drugId, Location location, UUID playerUUID, long plantingTime, String quality) {
        this.drugId = drugId;
        this.location = location.clone();
        this.location.setPitch(0);
        this.location.setYaw(0);
        this.playerUUID = playerUUID;
        this.plantingTime = plantingTime;
        this.quality = quality;
        this.age = 0;
    }

    public String getDrugId() {
        return drugId;
    }

    public Location getLocation() {
        return location.clone();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getPlantingTime() {
        return plantingTime;
    }

    public String getQuality() {
        return quality;
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
        this.age = age;
    }
}