package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Drug {
    private final String drugId;
    private final ItemStack item;
    private final ItemStack seed;
    private final int growthTime;
    private final double buyPrice;
    private final double sellPrice;
    private final String quality;
    private String hologramId;
    private int age;

    public Drug(String drugId, Material type, String name, int customModelData, List<String> lore, ItemStack seed,
                int growthTime, double buyPrice, double sellPrice, String quality) {
        this.drugId = drugId;
        this.item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        if (customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        this.seed = seed;
        this.growthTime = growthTime;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.quality = quality;
        this.age = 0;
    }

    public String getDrugId() {
        return drugId;
    }

    public ItemStack getItem(String quality) {
        return item.clone();
    }

    public ItemStack getSeedItem(String quality) {
        return seed != null ? seed.clone() : null;
    }

    public boolean hasSeed() {
        return seed != null;
    }

    public ItemStack getSeed() {
        return seed;
    }

    public int getGrowthTime() {
        return growthTime;
    }

    public double getBuyPrice(String quality) {
        return switch (quality) {
            case "Legendary" -> buyPrice * 2.0;
            case "Prime" -> buyPrice * 1.8;
            case "Exotic" -> buyPrice * 1.5;
            case "Standard" -> buyPrice * 1.2;
            default -> buyPrice;
        };
    }

    public double getSellPrice(String quality) {
        return switch (quality) {
            case "Legendary" -> sellPrice * 2.0;
            case "Prime" -> sellPrice * 1.8;
            case "Exotic" -> sellPrice * 1.5;
            case "Standard" -> sellPrice * 1.2;
            default -> sellPrice;
        };
    }

    public String getQuality() {
        return quality;
    }

    public String getName() {
        return item.getItemMeta().getDisplayName();
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