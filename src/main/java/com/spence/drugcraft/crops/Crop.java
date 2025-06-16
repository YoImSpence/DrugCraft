package com.spence.drugcraft.crops;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class Crop {
    private final String drugId;
    private final String quality;
    private final Location location;
    private final UUID playerUUID;
    private long plantingTime;
    private double age;

    public Crop(String drugId, String quality, Location location, UUID playerUUID) {
        this(drugId, quality, location, playerUUID, 0.0);
    }

    public Crop(String drugId, String quality, Location location, UUID playerUUID, double age) {
        this.drugId = drugId;
        this.quality = quality;
        this.location = location;
        this.playerUUID = playerUUID;
        this.plantingTime = System.currentTimeMillis();
        // Validate plantingTime
        long currentTime = System.currentTimeMillis();
        if (this.plantingTime <= 0 || Math.abs(this.plantingTime - currentTime) > 60000) { // Allow 60 seconds difference
            System.out.println("Invalid planting time " + this.plantingTime + " during crop creation at " + location.toString() + "; resetting to current time");
            this.plantingTime = currentTime;
        }
        this.age = age;
        System.out.println("Created crop at " + location.toString() + " with planting time: " + this.plantingTime);
    }

    public Crop(ConfigurationSection section) {
        this.drugId = section.getString("drug_id");
        this.quality = section.getString("quality");
        this.location = (Location) section.get("location");
        String uuidString = section.getString("player_uuid");
        this.playerUUID = uuidString != null ? UUID.fromString(uuidString) : null;
        this.plantingTime = section.getLong("planting_time");
        // Validate plantingTime
        long currentTime = System.currentTimeMillis();
        if (plantingTime <= 0 || Math.abs(plantingTime - currentTime) > BASE_GROWTH_TIME_MS * 2) { // Allow up to twice the base growth time
            System.out.println("Invalid planting time " + plantingTime + " for crop at " + (location != null ? location.toString() : "null") + "; resetting to current time");
            this.plantingTime = currentTime;
        }
        this.age = section.getDouble("age");
        System.out.println("Loaded crop at " + (location != null ? location.toString() : "null") + " with planting time: " + plantingTime);
        System.out.println("Loaded crop data - drug_id: " + drugId + ", quality: " + quality + ", age: " + age);
    }

    public void save(ConfigurationSection section) {
        section.set("drug_id", drugId);
        section.set("quality", quality);
        section.set("location", location);
        section.set("player_uuid", playerUUID != null ? playerUUID.toString() : null);
        section.set("planting_time", plantingTime);
        section.set("age", age);
        System.out.println("Saved crop at " + (location != null ? location.toString() : "null") + " with planting time: " + plantingTime);
        System.out.println("Saved crop data - drug_id: " + drugId + ", quality: " + quality + ", age: " + age);
    }

    public String getDrugId() {
        return drugId;
    }

    public String getQuality() {
        return quality;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getPlantingTime() {
        return plantingTime;
    }

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public double getCurrentGrowth() {
        return age;
    }

    public void setCurrentGrowth(double growth) {
        this.age = growth;
    }

    public void incrementGrowth(double amount) {
        this.age += amount;
    }

    private static final long BASE_GROWTH_TIME_MS = 300_000; // 5 minutes in milliseconds
}