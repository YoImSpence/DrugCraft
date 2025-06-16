package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class Drug {
    private final String id;
    private final String name;
    private final double price;
    private final double addictionChance;
    private final Material material;
    private final boolean hasSeed;
    private final Material seedMaterial;
    private final String description;

    public Drug(String id, ConfigurationSection config) {
        this.id = id;
        this.name = config.getString("name", id);
        this.price = config.getDouble("price", 100.0);
        this.addictionChance = config.getDouble("addiction-chance", 0.1);
        this.material = Material.valueOf(config.getString("material", "SUGAR").toUpperCase());
        this.hasSeed = config.getBoolean("has-seed", false);
        this.seedMaterial = hasSeed ? Material.valueOf(config.getString("seed-material", "WHEAT_SEEDS").toUpperCase()) : null;
        this.description = config.getString("description", "");
    }

    public Drug(String id, double price, double addictionChance, Material material) {
        this.id = id;
        this.name = id;
        this.price = price;
        this.addictionChance = addictionChance;
        this.material = material;
        this.hasSeed = false;
        this.seedMaterial = null;
        this.description = "";
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

    public double getAddictionChance() {
        return addictionChance;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean hasSeed() {
        return hasSeed;
    }

    public Material getSeedMaterial() {
        return seedMaterial;
    }

    public String getDescription() {
        return description;
    }
}