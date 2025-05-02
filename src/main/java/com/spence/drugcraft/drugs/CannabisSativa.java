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
 * Represents the Cannabis Sativa drug.
 */
public class CannabisSativa extends Drug {
    private static final NamespacedKey TYPE_KEY = new NamespacedKey("drugcraft", "type");

    public CannabisSativa() {
        super("Cannabis Sativa", "cannabis_sativa", Material.SUGAR, 50.0, 0.3, 5.0, "§a",
                new PotionEffect(PotionEffectType.SPEED, 600, 1),
                new PotionEffect(PotionEffectType.JUMP_BOOST, 600, 1));
    }

    @Override
    protected ItemStack createSeedItem() {
        ItemStack seed = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = seed.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aSativa Seed");
            meta.setLore(Arrays.asList("§7Plant to grow Cannabis Sativa"));
            seed.setItemMeta(meta);
        }
        try {
            DrugCraft plugin = (DrugCraft) Bukkit.getPluginManager().getPlugin("DrugCraft");
            if (plugin != null) {
                plugin.getLogger().fine("Attempting to create NBTItem for Cannabis Sativa seed");
            }
            NBTItem nbtItem = new NBTItem(seed);
            nbtItem.setString("drugcraft:type", "cannabis_sativa");
            ItemStack result = nbtItem.getItem();
            if (plugin != null) {
                plugin.getLogger().fine("Successfully set NBT tag 'drugcraft:type=cannabis_sativa' for Cannabis Sativa seed");
            }
            return result;
        } catch (Throwable e) {
            DrugCraft plugin = (DrugCraft) Bukkit.getPluginManager().getPlugin("DrugCraft");
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to set NBT tag for Cannabis Sativa seed: " + e.getMessage(), e);
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to set NBT tag for Cannabis Sativa seed and plugin is null: " + e.getMessage(), e);
            }
            // Fallback to PersistentDataContainer
            if (meta != null) {
                meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, "cannabis_sativa");
                seed.setItemMeta(meta);
                if (plugin != null) {
                    plugin.getLogger().warning("Fell back to PersistentDataContainer for Cannabis Sativa seed");
                }
            }
            return seed;
        }
    }
}