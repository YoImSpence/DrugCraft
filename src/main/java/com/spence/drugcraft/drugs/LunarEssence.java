package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents the Lunar Essence drug.
 */
public class LunarEssence extends Drug {
    public LunarEssence() {
        super("Lunar Essence", "lunar_essence", Material.PHANTOM_MEMBRANE, 80.0, 0.6, 8.0, "§f",
                new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1),
                new PotionEffect(PotionEffectType.SLOW_FALLING, 600, 1));
    }
}