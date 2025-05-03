package com.spence.drugcraft.drugs;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Drug {
    protected final String id;
    protected final String name;
    protected final Material material;
    protected final List<String> lore;
    protected final List<PotionEffect> effects;
    protected final double price;
    protected final boolean hasSeed;
    protected final Material seedMaterial;
    protected final int growthTime;
    protected ItemStack item;
    protected ItemStack seedItem;
    protected final Logger logger;

    public Drug(String id, String name, Material material, List<String> lore, List<PotionEffect> effects, double price,
                boolean hasSeed, Material seedMaterial, int growthTime, Logger logger) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.lore = lore;
        this.effects = effects;
        this.price = price;
        this.hasSeed = hasSeed;
        this.seedMaterial = seedMaterial;
        this.growthTime = growthTime;
        this.logger = logger;
        this.item = createItem();
        if (hasSeed) {
            this.seedItem = createSeedItem();
        }
    }

    protected ItemStack createItem() {
        try {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(lore);
                item.setItemMeta(meta);
                NBTItem nbtItem = new NBTItem(item);
                nbtItem.setString("drug_id", id);
                return nbtItem.getItem();
            }
        } catch (Exception e) {
            logger.severe("Failed to create item for drug '" + name + "': " + e.getMessage());
        }
        return new ItemStack(material);
    }

    protected ItemStack createSeedItem() {
        try {
            ItemStack seed = new ItemStack(seedMaterial);
            ItemMeta meta = seed.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name + " Seed");
                List<String> seedLore = new ArrayList<>(lore);
                seedLore.add("Plant on farmland to grow.");
                meta.setLore(seedLore);
                seed.setItemMeta(meta);
                NBTItem nbtItem = new NBTItem(seed);
                nbtItem.setString("seed_id", id + "_seed");
                return nbtItem.getItem();
            }
        } catch (Exception e) {
            logger.severe("Failed to create seed item for drug '" + name + "': " + e.getMessage());
        }
        return new ItemStack(seedMaterial);
    }

    public void use(Player player) {
        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }
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

    public boolean hasSeed() {
        return hasSeed;
    }

    public int getGrowthTime() {
        return growthTime;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public ItemStack getSeedItem() {
        return seedItem != null ? seedItem.clone() : null;
    }
}