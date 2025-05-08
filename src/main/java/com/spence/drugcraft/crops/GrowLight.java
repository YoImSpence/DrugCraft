package com.spence.drugcraft.crops;

import com.spence.drugcraft.utils.MessageUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GrowLight {
    private static final Map<Location, String> holograms = new HashMap<>();
    private static final Map<Location, ArmorStand> growLights = new HashMap<>();
    private static final NamespacedKey QUALITY_KEY = new NamespacedKey("drugcraft", "grow_light_quality");
    private static final NamespacedKey GROW_LIGHT_KEY = new NamespacedKey("drugcraft", "is_grow_light");

    public static ItemStack createGrowLightItem(String quality) {
        ItemStack growLight = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = growLight.getItemMeta();
        String displayName = switch (quality) {
            case "Legendary" -> "&#FFD700Legendary Grow Light";
            case "Prime" -> "&#1E90FFPrime Grow Light";
            case "Exotic" -> "&#FF4500Exotic Grow Light";
            case "Standard" -> "&#00FF00Standard Grow Light";
            default -> "&#00FFFFBasic Grow Light";
        };
        meta.setDisplayName(MessageUtils.color(displayName));
        meta.setLore(Arrays.asList(
                MessageUtils.color(getQualityColor(quality) + "Quality: " + quality),
                MessageUtils.color("&#D3D3D3Place above crops to boost growth speed"),
                MessageUtils.color("&#FFFF00Shift+Right Click to pick up")
        ));
        meta.setCustomModelData(3000);
        meta.getPersistentDataContainer().set(QUALITY_KEY, PersistentDataType.STRING, quality);
        meta.getPersistentDataContainer().set(GROW_LIGHT_KEY, PersistentDataType.BYTE, (byte) 1);
        growLight.setItemMeta(meta);
        return growLight;
    }

    public static ArmorStand placeGrowLight(Location location, String quality) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        ItemStack lightItem = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = lightItem.getItemMeta();
        meta.setCustomModelData(3000);
        lightItem.setItemMeta(meta);
        armorStand.getEquipment().setHelmet(lightItem);
        armorStand.setGlowing(true);

        PersistentDataContainer container = armorStand.getPersistentDataContainer();
        container.set(QUALITY_KEY, PersistentDataType.STRING, quality);
        container.set(GROW_LIGHT_KEY, PersistentDataType.BYTE, (byte) 1);

        growLights.put(location, armorStand);
        createHologram(location, quality);
        return armorStand;
    }

    public static String getQualityAtLocation(Location location) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
            if (entity instanceof ArmorStand armorStand && isGrowLight(armorStand)) {
                PersistentDataContainer container = armorStand.getPersistentDataContainer();
                return container.get(QUALITY_KEY, PersistentDataType.STRING);
            }
        }
        return null;
    }

    public static void removeGrowLight(Location location) {
        ArmorStand armorStand = growLights.remove(location);
        if (armorStand != null) {
            armorStand.remove();
        }
        removeHologram(location);
    }

    public static boolean isGrowLight(ArmorStand armorStand) {
        PersistentDataContainer container = armorStand.getPersistentDataContainer();
        return container.has(GROW_LIGHT_KEY, PersistentDataType.BYTE) &&
                container.get(GROW_LIGHT_KEY, PersistentDataType.BYTE) == 1;
    }

    public static boolean isGrowLightItem(ItemStack item) {
        if (item == null || item.getType() != Material.GLOWSTONE_DUST || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(GROW_LIGHT_KEY, PersistentDataType.BYTE) &&
                container.get(GROW_LIGHT_KEY, PersistentDataType.BYTE) == 1;
    }

    public static String getQualityFromGrowLight(ItemStack item) {
        if (item == null || item.getType() != Material.GLOWSTONE_DUST || !item.hasItemMeta()) {
            return "Basic";
        }
        ItemMeta meta = item.getItemMeta();
        String quality = meta.getPersistentDataContainer().get(QUALITY_KEY, PersistentDataType.STRING);
        return quality != null ? quality : "Basic";
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
            case "Legendary" -> "&#FFD700";
            case "Prime" -> "&#1E90FF";
            case "Exotic" -> "&#FF4500";
            case "Standard" -> "&#00FF00";
            default -> "&#00FFFF";
        };
        DHAPI.setHologramLines(hologram, Arrays.asList(
                MessageUtils.color("&#FFDAB9" + quality + " Grow Light"),
                MessageUtils.color("&#D3D3D3Boosting crop growth")
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
            case "Legendary" -> "&#FFD700";
            case "Prime" -> "&#1E90FF";
            case "Exotic" -> "&#FF4500";
            case "Standard" -> "&#00FF00";
            default -> "&#00FFFF";
        };
    }
}