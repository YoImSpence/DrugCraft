package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class CannabisIndica extends Drug {
    public CannabisIndica(Logger logger) {
        super("cannabis_indica", "&aCannabis Indica", Material.SUGAR,
                Arrays.asList("&7A strain that promotes relaxation."),
                Arrays.asList(new PotionEffect(PotionEffectType.SLOWNESS, 60 * 20, 0)),
                20.0, true, Material.WHEAT_SEEDS, 3600, logger);
    }
}