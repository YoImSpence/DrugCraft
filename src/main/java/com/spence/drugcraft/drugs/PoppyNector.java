package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents the Poppy Nector drug.
 */
public class PoppyNector extends Drug {
    public PoppyNector() {
        super("Poppy Nector", "poppy_nector", Material.HONEY_BOTTLE, 75.0, 0.55, 7.5, "§c",
                new PotionEffect(PotionEffectType.RESISTANCE, 600, 1),
                new PotionEffect(PotionEffectType.WEAKNESS, 600, 1));
    }
}