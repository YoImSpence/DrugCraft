package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class LunarEssence extends Drug {
    public LunarEssence(Logger logger) {
        super("lunar_essence", "&bLunar Essence", Material.PHANTOM_MEMBRANE,
                Arrays.asList("&7Mystical essence that enhances luck."),
                Arrays.asList(new PotionEffect(PotionEffectType.LUCK, 90 * 20, 1)),
                25.0, false, null, 0, logger);
    }
}