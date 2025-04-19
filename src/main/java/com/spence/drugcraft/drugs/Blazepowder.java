package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class Blazepowder extends Drug {
    public Blazepowder(double sellPrice, int addictionStrength) {
        super("Blazepowder", createItem(), addictionStrength, sellPrice);
    }

    private static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cBlazepowder");
            meta.setLore(Arrays.asList("§7A fiery stimulant", "§7Grants speed and strength"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1)); // 30s Speed II
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 0)); // 30s Strength I
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0)); // 10s Nausea
        player.sendMessage("§cYou feel a rush of energy!");
    }

    @Override
    public List<PotionEffect> getEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.SPEED, 600, 1), // 30s Speed II
                new PotionEffect(PotionEffectType.STRENGTH, 600, 0), // 30s Strength I
                new PotionEffect(PotionEffectType.NAUSEA, 200, 0) // 10s Nausea
        );
    }
}