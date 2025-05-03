package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class CannabisHybrid extends Drug {
    public CannabisHybrid(Logger logger) {
        super("cannabis_hybrid", "&aCannabis Hybrid", Material.SUGAR,
                Arrays.asList("&7A balanced strain with mixed effects."),
                Arrays.asList(new PotionEffect(PotionEffectType.REGENERATION, 60 * 20, 0)),
                20.0, true, Material.WHEAT_SEEDS, 3600, logger);
    }
}