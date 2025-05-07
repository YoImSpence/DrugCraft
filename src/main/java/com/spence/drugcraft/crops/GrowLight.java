package com.spence.drugcraft.crops;

import com.spence.drugcraft.utils.MessageUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GrowLight {
    private static final Map<Location, String> holograms = new HashMap<>();

    public static ItemStack createGrowLight(String quality) {
        ItemStack growLight = new ItemStack(Material.OCHRE_FROGLIGHT);
        ItemMeta meta = growLight.getItemMeta();
        String displayName = switch (quality) {
            case "Legendary" -> "&dLegendary Grow Light";
            case "Prime" -> "&9Prime Grow Light";
            case "Exotic" -> "&eExotic Grow Light";
            case "Standard" -> "&aStandard Grow Light";
            default -> "&bBasic Grow Light";
        };
        meta.setDisplayName(MessageUtils.color(displayName));
        meta.setLore(Arrays.asList(
                MessageUtils.color(getQualityColor(quality) + "Quality: " + quality),
                MessageUtils.color("&7Place above crops to boost growth speed")
        ));
        meta.setCustomModelData(3000);
        growLight.setItemMeta(meta);
        return growLight;
    }

    public static boolean isGrowLight(ItemStack item) {
        if (item == null || item.getType() != Material.OCHRE_FROGLIGHT || !item.hasItemMeta()) {
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

    public static void createHologram(Location location, String quality) {
        String hologramId = "growlight_" + UUID.randomUUID();
        Location hologramLoc = location.clone().add(0.5, 1.5, 0.5);
        hologramLoc.setPitch(0);
        hologramLoc.setYaw(0);
        Hologram hologram = DHAPI.createHologram(hologramId, hologramLoc);
        if (hologram == null) {
            return;
        }
        String color = switch (quality) {
            case "Legendary" -> "&d";
            case "Prime" -> "&9";
            case "Exotic" -> "&e";
            case "Standard" -> "&a";
            default -> "&b";
        };
        DHAPI.setHologramLines(hologram, Arrays.asList(
                MessageUtils.color(color + quality + " Grow Light"),
                MessageUtils.color("&7Boosting crop growth")
        ));
        holograms.put(location, hologramId);
    }

    public static void removeHologram(Location location) {
        String hologramId = holograms.remove(location);
        if (hologramId != null) {
            Hologram hologram = DHAPI.getHologram(hologramId);
            if (hologram != null) {
                hologram.delete();
            }
        }
    }

    private static String getQualityColor(String quality) {
        return switch (quality) {
            case "Legendary" -> "&d";
            case "Prime" -> "&9";
            case "Exotic" -> "&e";
            case "Standard" -> "&a";
            default -> "&b";
        };
    }
}