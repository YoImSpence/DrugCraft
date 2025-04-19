package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class MysticShroom extends Drug {
    public MysticShroom(double sellPrice, int addictionStrength) {
        super("MysticShroom", createItem(), addictionStrength, sellPrice);
    }

    private static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.RED_MUSHROOM);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Mystic Shroom");
            meta.setLore(Arrays.asList("§7A psychedelic mushroom", "§7Alters perception"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 400, 0)); // 20s Nausea
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0)); // 10s Blindness
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0)); // 20s Night Vision
        player.sendMessage("§5The world shifts around you...");
    }

    @Override
    public List<PotionEffect> getEffects() {
        return Arrays.asList(
                new PotionEffect(PotionEffectType.NAUSEA, 400, 0), // 20s Nausea
                new PotionEffect(PotionEffectType.BLINDNESS, 200, 0), // 10s Blindness
                new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0) // 20s Night Vision
        );
    }
}