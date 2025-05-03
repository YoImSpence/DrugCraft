package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class Cannabis extends Drug {
    public Cannabis(Logger logger) {
        super("cannabis", "&aCannabis", Material.SUGAR,
                Arrays.asList("&7A versatile plant with various effects."),
                Arrays.asList(new PotionEffect(PotionEffectType.REGENERATION, 60 * 20, 0)),
                15.0, true, Material.WHEAT_SEEDS, 3600, logger);
    }
}