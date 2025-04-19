package com.spence.drugcraft.drugs;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public abstract class Drug {
    private final String name;
    private final ItemStack item;
    private final int addictionStrength;
    private final double sellPrice;

    public Drug(String name, ItemStack item, int addictionStrength, double sellPrice) {
        this.name = name;
        this.item = item.clone(); // Clone on construction
        this.addictionStrength = addictionStrength;
        this.sellPrice = sellPrice;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item.clone(); // Always return a clone
    }

    public int getAddictionStrength() {
        return addictionStrength;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public abstract void applyEffect(Player player);

    public abstract List<PotionEffect> getEffects();
}