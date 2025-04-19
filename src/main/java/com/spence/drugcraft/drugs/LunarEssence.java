package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class LunarEssence extends Drug {
    public LunarEssence(double sellPrice, int addictionStrength) {
        super("LunarEssence", createItem(), addictionStrength, sellPrice);
    }

    private static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bLunar Essence");
            meta.setLore(Arrays.asList("§7A calming elixir", "§7Relaxes but dulls senses"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 0)); // 20s Slowness I
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 0)); // 20s Weakness I
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 400, 0)); // 20s Resistance I
        player.sendMessage("§bYou feel deeply relaxed...");
    }

    @Override
    public List<PotionEffect> getEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.SLOWNESS, 400, 0), // 20s Slowness I
                new PotionEffect(PotionEffectType.WEAKNESS, 400, 0), // 20s Weakness I
                new PotionEffect(PotionEffectType.RESISTANCE, 400, 0) // 20s Resistance I
        );
    }
}