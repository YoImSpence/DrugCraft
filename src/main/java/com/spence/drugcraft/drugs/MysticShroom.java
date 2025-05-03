package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.logging.Logger;

public class MysticShroom extends Drug {
    public MysticShroom(Logger logger) {
        super("mystic_shroom", "&dMystic Shroom", Material.RED_MUSHROOM,
                Arrays.asList("&7Causes vivid hallucinations."),
                Arrays.asList(new PotionEffect(PotionEffectType.NAUSEA, 45 * 20, 0)),
                12.0, false, null, 0, logger);
    }
}