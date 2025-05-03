package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class GlowvineExtract extends Drug {
    public GlowvineExtract(Logger logger) {
        super("glowvine_extract", "&eGlowvine Extract", Material.GLOWSTONE_DUST,
                Arrays.asList("&7Grants night vision."),
                Arrays.asList(new PotionEffect(PotionEffectType.NIGHT_VISION, 120 * 20, 0)),
                15.0, false, null, 0, logger);
    }
}