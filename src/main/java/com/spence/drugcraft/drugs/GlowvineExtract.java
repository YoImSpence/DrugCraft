package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents the Glowvine Extract drug.
 */
public class GlowvineExtract extends Drug {
    public GlowvineExtract() {
        super("Glowvine Extract", "glowvine_extract", Material.GLOWSTONE_DUST, 70.0, 0.5, 7.0, "§e",
                new PotionEffect(PotionEffectType.GLOWING, 600, 1),
                new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 1));
    }
}