package com.spence.drugcraft.steeds;

public class Steed {
    private final String id;
    private final double speed;
    private final double health;

    public Steed(String id, double speed, double health) {
        this.id = id;
        this.speed = speed;
        this.health = health;
    }

    public String getId() {
        return id;
    }

    public double getSpeed() {
        return speed;
    }

    public double getHealth() {
        return health;
    }
}