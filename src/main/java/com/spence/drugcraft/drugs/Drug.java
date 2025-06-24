package com.spence.drugcraft.drugs;

public class Drug {
    private final String id;
    private final String name;
    private final double price;
    private final boolean growable;
    private final double strength;
    private final int duration;

    public Drug(String id, String name, double price, boolean growable, double strength, int duration) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.growable = growable;
        this.strength = strength;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean isGrowable() {
        return growable;
    }

    public double getStrength() {
        return strength;
    }

    public int getDuration() {
        return duration;
    }
}