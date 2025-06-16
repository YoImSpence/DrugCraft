package com.spence.drugcraft.businesses;

import java.util.*;

public class Business {
    private final String id;
    private final String name;
    private UUID ownerUUID;
    private double revenue;
    private final double price;
    private final String regionId;
    private final List<String> allowedDrugs;

    public Business(String id, String name, UUID ownerUUID, double revenue, double price, String regionId, List<String> allowedDrugs) {
        this.id = id;
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.revenue = revenue;
        this.price = price;
        this.regionId = regionId;
        this.allowedDrugs = allowedDrugs;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getPrice() {
        return price;
    }

    public String getRegionId() {
        return regionId;
    }

    public List<String> getAllowedDrugs() {
        return allowedDrugs;
    }

    public String getType() {
        return name.toLowerCase();
    }

    public int getRequiredLevel() {
        return switch (name.toLowerCase()) {
            case "dispensary" -> 5;
            case "car wash" -> 7;
            case "grow house" -> 10;
            case "smuggler’s den" -> 15;
            default -> 1;
        };
    }
}