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
 * Represents the Cannabis Indica drug.
 */
public class CannabisIndica extends Drug {
    private static final NamespacedKey TYPE_KEY = new NamespacedKey("drugcraft", "type");

    public CannabisIndica() {
        super("Cannabis Indica", "cannabis_indica", Material.SUGAR, 60.0, 0.4, 6.0, "§b",
                new PotionEffect(PotionEffectType.SLOWNESS, 600, 1),
                new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
    }

    @Override
    protected ItemStack createSeedItem() {
        ItemStack seed = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = seed.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bIndica Seed");
            meta.setLore(Arrays.asList("§7Plant to grow Cannabis Indica"));
            seed.setItemMeta(meta);
        }
        try {
            DrugCraft plugin = (DrugCraft) Bukkit.getPluginManager().getPlugin("DrugCraft");
            if (plugin != null) {
                plugin.getLogger().fine("Attempting to create NBTItem for Cannabis Indica seed");
            }
            NBTItem nbtItem = new NBTItem(seed);
            nbtItem.setString("drugcraft:type", "cannabis_indica");
            ItemStack result = nbtItem.getItem();
            if (plugin != null) {
                plugin.getLogger().fine("Successfully set NBT tag 'drugcraft:type=cannabis_indica' for Cannabis Indica seed");
            }
            return result;
        } catch (Throwable e) {
            DrugCraft plugin = (DrugCraft) Bukkit.getPluginManager().getPlugin("DrugCraft");
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to set NBT tag for Cannabis Indica seed: " + e.getMessage(), e);
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to set NBT tag for Cannabis Indica seed and plugin is null: " + e.getMessage(), e);
            }
            // Fallback to PersistentDataContainer
            if (meta != null) {
                meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, "cannabis_indica");
                seed.setItemMeta(meta);
                if (plugin != null) {
                    plugin.getLogger().warning("Fell back to PersistentDataContainer for Cannabis Indica seed");
                }
            }
            return seed;
        }
    }
}