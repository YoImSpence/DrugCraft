package com.spence.drugcraft.deals;

import org.bukkit.Location;

public class AcceptedDeal {
    private final int citizenId;
    private final String drugId;
    private final String drugName;
    private final String quality;
    private final int amount;
    private double price; // Made mutable for price negotiation
    private final Location meetupSpot;

    public AcceptedDeal(int citizenId, String drugId, String drugName, String quality, int amount, double price, Location meetupSpot) {
        this.citizenId = citizenId;
        this.drugId = drugId;
        this.drugName = drugName;
        this.quality = quality;
        this.amount = amount;
        this.price = price;
        this.meetupSpot = meetupSpot;
    }

    public int getCitizenId() {
        return citizenId;
    }

    public String getDrugId() {
        return drugId;
    }

    public String getDrugName() {
        return drugName;
    }

    public String getQuality() {
        return quality;
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Location getMeetupSpot() {
        return meetupSpot;
    }
}