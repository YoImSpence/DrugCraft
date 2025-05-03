package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class PoppyNector extends Drug {
    public PoppyNector(Logger logger) {
        super("poppy_nector", "&cPoppy Nector", Material.HONEY_BOTTLE,
                Arrays.asList("&7Soothes pain but dulls senses."),
                Arrays.asList(
                        new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60 * 20, 0),
                        new PotionEffect(PotionEffectType.SLOWNESS, 60 * 20, 0)
                ),
                18.0, false, null, 0, logger);
    }
}