package com.spence.drugcraft.businesses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Business {
    private UUID ownerUUID;
    private final String id;
    private final String name;
    private final String type;
    private final String regionId;
    private final Map<String, Integer> upgrades;
    private double revenue;
    private double price;
    private final List<String> allowedDrugs;
    private final int requiredLevel;

    public Business(UUID ownerUUID, String id, String name, String type, String regionId, int requiredLevel) {
        this.ownerUUID = ownerUUID;
        this.id = id;
        this.name = name;
        this.type = type;
        this.regionId = regionId;
        this.upgrades = new HashMap<>();
        this.revenue = 0.0;
        this.price = 5000.0;
        this.allowedDrugs = new ArrayList<>();
        this.requiredLevel = requiredLevel;
    }

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getRegionId() { return regionId; }
    public Map<String, Integer> getUpgrades() { return new HashMap<>(upgrades); }
    public double getRevenue() { return revenue; }
    public double getPrice() { return price; }
    public List<String> getAllowedDrugs() { return new ArrayList<>(allowedDrugs); }
    public int getRequiredLevel() { return requiredLevel; }

    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public void setUpgrade(String upgrade, int level) { upgrades.put(upgrade, level); }
    public void setRevenue(double revenue) { this.revenue = revenue; }
    public void setPrice(double price) { this.price = price; }
    public void addAllowedDrug(String drugId) { allowedDrugs.add(drugId); }
}