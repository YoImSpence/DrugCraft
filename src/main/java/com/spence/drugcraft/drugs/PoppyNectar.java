package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class PoppyNectar extends Drug {
    public PoppyNectar(double sellPrice, int addictionStrength) {
        super("PoppyNectar", createItem(), addictionStrength, sellPrice);
    }

    private static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§dPoppy Nectar");
            meta.setLore(Arrays.asList("§7A soothing elixir", "§7Grants euphoria but slows you"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1)); // 30s Regeneration II
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 0)); // 30s Slowness I
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 0)); // 30s Weakness I
        player.sendMessage("§dYou feel a warm euphoria...");
    }

    @Override
    public List<PotionEffect> getEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.REGENERATION, 600, 1), // 30s Regeneration II
                new PotionEffect(PotionEffectType.SLOWNESS, 600, 0), // 30s Slowness I
                new PotionEffect(PotionEffectType.WEAKNESS, 600, 0) // 30s Weakness I
        );
    }
}