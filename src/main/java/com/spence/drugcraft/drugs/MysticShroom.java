package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents the Mystic Shroom drug.
 */
public class MysticShroom extends Drug {
    public MysticShroom() {
        super("Mystic Shroom", "mystic_shroom", Material.RED_MUSHROOM, 65.0, 0.45, 6.5, "§d",
                new PotionEffect(PotionEffectType.NAUSEA, 600, 1),
                new PotionEffect(PotionEffectType.HEALTH_BOOST, 600, 1));
    }
}