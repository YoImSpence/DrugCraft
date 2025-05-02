package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents the Blazepowder drug.
 */
public class Blazepowder extends Drug {
    public Blazepowder() {
        super("Blazepowder", "blazepowder", Material.BLAZE_POWDER, 40.0, 0.2, 4.0, "§6",
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 1),
                new PotionEffect(PotionEffectType.STRENGTH, 600, 1));
    }
}