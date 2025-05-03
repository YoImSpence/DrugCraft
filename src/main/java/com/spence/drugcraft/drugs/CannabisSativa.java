package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class CannabisSativa extends Drug {
    public CannabisSativa(Logger logger) {
        super("cannabis_sativa", "&aCannabis Sativa", Material.SUGAR,
                Arrays.asList("&7A strain known for uplifting effects."),
                Arrays.asList(new PotionEffect(PotionEffectType.JUMP_BOOST, 60 * 20, 0)),
                20.0, true, Material.WHEAT_SEEDS, 3600, logger);
    }
}