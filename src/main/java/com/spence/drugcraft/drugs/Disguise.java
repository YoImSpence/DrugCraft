package com.spence.drugcraft.drugs;

import org.bukkit.inventory.ItemStack;

public class Disguise {
    public static boolean isDisguise(ItemStack item) {
        // Placeholder: Implement disguise item check
        return item != null && item.getType() == org.bukkit.Material.LEATHER_HELMET;
    }
}