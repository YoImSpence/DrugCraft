package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class Cannabis extends Drug {
    public Cannabis(double sellPrice, int addictionStrength) {
        super("Cannabis", createItem(), addictionStrength, sellPrice);
    }

    private static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.DRIED_KELP);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aCannabis");
            meta.setLore(Arrays.asList("§7A relaxing herb", "§7Grants mild effects"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0)); // 20s Regeneration I
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 0)); // 20s Slowness I
        player.sendMessage("§aYou feel relaxed...");
    }

    @Override
    public List<PotionEffect> getEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.REGENERATION, 400, 0),
                new PotionEffect(PotionEffectType.SLOWNESS, 400, 0)
        );
    }
}