package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class GlowvineExtract extends Drug {
    public GlowvineExtract(double sellPrice, int addictionStrength) {
        super("GlowvineExtract", createItem(), addictionStrength, sellPrice);
    }

    private static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.GLOW_BERRIES);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eGlowvine Extract");
            meta.setLore(Arrays.asList("§7A vibrant extract", "§7Boosts teamwork and energy"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 600, 1)); // 30s Haste II
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0)); // 30s Glowing
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0)); // 10s Nausea
        player.sendMessage("§eYou feel connected and energized!");
    }

    @Override
    public List<PotionEffect> getEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.HASTE, 600, 1), // 30s Haste II
                new PotionEffect(PotionEffectType.GLOWING, 600, 0), // 30s Glowing
                new PotionEffect(PotionEffectType.NAUSEA, 200, 0) // 10s Nausea
        );
    }
}