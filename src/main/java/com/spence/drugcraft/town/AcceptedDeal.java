package com.spence.drugcraft.town;

import org.bukkit.Location;

public class AcceptedDeal {
    private final int npcId;
    private final String npcName;
    private final String drugId;
    private final String quality;
    private final int quantity;
    private final double price;
    private final Location meetupLocation;

    public AcceptedDeal(int npcId, String npcName, String drugId, String quality, int quantity, double price, Location meetupLocation) {
        this.npcId = npcId;
        this.npcName = npcName;
        this.drugId = drugId;
        this.quality = quality;
        this.quantity = quantity;
        this.price = price;
        this.meetupLocation = meetupLocation;
    }

    public int getNpcId() { return npcId; }
    public String getNpcName() { return npcName; }
    public String getDrugId() { return drugId; }
    public String getQuality() { return quality; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Location getMeetupLocation() { return meetupLocation; }
}