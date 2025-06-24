package com.spence.drugcraft.drugs;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ScentBlocker {
    public static boolean isScentBlocker(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() >= 1 && meta.getCustomModelData() <= 5;
    }

    public static String getQuality(ItemStack item) {
        if (!isScentBlocker(item)) return null;
        int customModelData = item.getItemMeta().getCustomModelData();
        return switch (customModelData) {
            case 1 -> "Standard";
            case 2 -> "Prime";
            case 3 -> "Exotic";
            case 4 -> "Legendary";
            case 5 -> "Cosmic";
            default -> "Basic";
        };
    }
}