package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * Represents the Cannabis Hybrid drug.
 */
public class CannabisHybrid extends Drug {
    private static final NamespacedKey TYPE_KEY = new NamespacedKey("drugcraft", "type");

    public CannabisHybrid() {
        super("Cannabis Hybrid", "cannabis_hybrid", Material.SUGAR, 55.0, 0.35, 5.5, "§c",
                new PotionEffect(PotionEffectType.SPEED, 400, 1),
                new PotionEffect(PotionEffectType.REGENERATION, 400, 1));
    }

    @Override
    protected ItemStack createSeedItem() {
        ItemStack seed = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = seed.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cHybrid Seed");
            meta.setLore(Arrays.asList("§7Plant to grow Cannabis Hybrid"));
            seed.setItemMeta(meta);
        }
        try {
            DrugCraft plugin = (DrugCraft) Bukkit.getPluginManager().getPlugin("DrugCraft");
            if (plugin != null) {
                plugin.getLogger().fine("Attempting to create NBTItem for Cannabis Hybrid seed");
            }
            NBTItem nbtItem = new NBTItem(seed);
            nbtItem.setString("drugcraft:type", "cannabis_hybrid");
            ItemStack result = nbtItem.getItem();
            if (plugin != null) {
                plugin.getLogger().fine("Successfully set NBT tag 'drugcraft:type=cannabis_hybrid' for Cannabis Hybrid seed");
            }
            return result;
        } catch (Throwable e) {
            DrugCraft plugin = (DrugCraft) Bukkit.getPluginManager().getPlugin("DrugCraft");
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to set NBT tag for Cannabis Hybrid seed: " + e.getMessage(), e);
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to set NBT tag for Cannabis Hybrid seed and plugin is null: " + e.getMessage(), e);
            }
            // Fallback to PersistentDataContainer
            if (meta != null) {
                meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, "cannabis_hybrid");
                seed.setItemMeta(meta);
                if (plugin != null) {
                    plugin.getLogger().warning("Fell back to PersistentDataContainer for Cannabis Hybrid seed");
                }
            }
            return seed;
        }
    }
}