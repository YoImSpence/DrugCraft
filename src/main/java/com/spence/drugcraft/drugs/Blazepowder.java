package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class Blazepowder extends Drug {
    public Blazepowder(Logger logger) {
        super("blazepowder", "&6Blaze Powder", Material.BLAZE_POWDER,
                Arrays.asList("&7A fiery substance that boosts speed."),
                Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1)),
                10.0, false, null, 0, logger);
    }
}