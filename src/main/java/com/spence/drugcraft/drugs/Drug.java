package com.spence.drugcraft.drugs;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Drug {
    private final String id;
    private final org.bukkit.Material type;
    private final String name;
    private final int customModelData;
    private final List<String> lore;
    private final ItemStack seed;
    private final int growthTime;
    private final double buyPrice;
    private final double sellPrice;
    private final String quality;

    public Drug(String id, org.bukkit.Material type, String name, int customModelData, List<String> lore,
                ItemStack seed, int growthTime, double buyPrice, double sellPrice, String quality) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.customModelData = customModelData;
        this.lore = lore;
        this.seed = seed;
        this.growthTime = growthTime;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.quality = quality;
    }

    public String getDrugId() {
        return id;
    }

    public org.bukkit.Material getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemStack getSeed() {
        return seed != null ? seed.clone() : null;
    }

    public boolean hasSeed() {
        return seed != null;
    }

    public int getGrowthTime() {
        return growthTime;
    }

    public double getBuyPrice(String quality) {
        return switch (quality) {
            case "Legendary" -> buyPrice * 2.5;
            case "Prime" -> buyPrice * 2.0;
            case "Exotic" -> buyPrice * 1.5;
            case "Standard" -> buyPrice * 1.2;
            default -> buyPrice;
        };
    }

    public double getSellPrice(String quality) {
        return switch (quality) {
            case "Legendary" -> sellPrice * 2.5;
            case "Prime" -> sellPrice * 2.0;
            case "Exotic" -> sellPrice * 1.5;
            case "Standard" -> sellPrice * 1.2;
            default -> sellPrice;
        };
    }

    public String getQuality() {
        return quality;
    }

    // Add missing getItem method
    public ItemStack getItem(String quality) {
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> newLore = new ArrayList<>(lore);
        meta.setLore(newLore);
        if (customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    // Add missing getSeedItem method
    public ItemStack getSeedItem(String quality) {
        if (!hasSeed()) return null;
        ItemStack seedItem = seed.clone();
        ItemMeta meta = seedItem.getItemMeta();
        List<String> newLore = new ArrayList<>(meta.getLore());
        meta.setLore(newLore);
        seedItem.setItemMeta(meta);
        return seedItem;
    }
}