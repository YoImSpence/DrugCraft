package com.spence.drugcraft.crops;

import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GrowLight {
    public static ItemStack createGrowLight(String quality) {
        ItemStack growLight = new ItemStack(Material.REDSTONE_LAMP);
        ItemMeta meta = growLight.getItemMeta();
        String displayName = switch (quality) {
            case "Legendary" -> "{#FF00FF}Legendary Grow Light";
            case "Prime" -> "{#1E90FF}Prime Grow Light";
            case "Exotic" -> "{#FFA500}Exotic Grow Light";
            case "Standard" -> "{#00FF00}Standard Grow Light";
            default -> "{#AAAAAA}Basic Grow Light";
        };
        meta.setDisplayName(MessageUtils.color(displayName));
        meta.setLore(Arrays.asList(
                MessageUtils.color(getQualityColor(quality) + "Quality: " + quality),
                MessageUtils.color("{#AAAAAA}Place above crops to boost growth speed")
        ));
        meta.setCustomModelData(3000); // Unique model data
        growLight.setItemMeta(meta);
        return growLight;
    }

    public static boolean isGrowLight(ItemStack item) {
        if (item == null || item.getType() != Material.REDSTONE_LAMP || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.hasLore() && meta.getLore().stream().anyMatch(line -> line.contains("Quality: "));
    }

    public static String getQualityFromGrowLight(ItemStack item) {
        if (!isGrowLight(item)) {
            return "Basic";
        }
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("Quality: ")) {
                return line.substring(line.indexOf("Quality: ") + 9);
            }
        }
        return "Basic";
    }

    private static String getQualityColor(String quality) {
        return switch (quality) {
            case "Legendary" -> "{#FF00FF}";
            case "Prime" -> "{#1E90FF}";
            case "Exotic" -> "{#FFA500}";
            case "Standard" -> "{#00FF00}";
            default -> "{#AAAAAA}"; // Basic
        };
    }
}