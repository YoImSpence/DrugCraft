package com.spence.drugcraft.crops;

import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GrowLight {
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private static final Map<Location, String> growLights = new HashMap<>();
    private static final Map<Location, String> hologramIds = new HashMap<>();

    public GrowLight(CropManager cropManager, DrugManager drugManager) {
        this.cropManager = cropManager;
        this.drugManager = drugManager;
    }

    public ItemStack createGrowLightItem(String quality) {
        ItemStack growLight = new ItemStack(Material.SEA_LANTERN);
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
                MessageUtils.color(qualityColors.getOrDefault(quality, "&#FFFFFF") + "Quality: " + quality),
                MessageUtils.color("&#D3D3D3Boosts crop growth when placed above")
        ));
        growLight.setItemMeta(meta);
        return growLight;
    }

    public boolean placeGrowLight(Location location, String quality) {
        if (growLights.containsKey(location)) return false;
        growLights.put(location, quality);
        createHologram(location, quality);
        return true;
    }

    private void createHologram(Location location, String quality) {
        String hologramId = "growlight_" + location.getWorld().getName() + "_" + location.getBlockX() + "_" +
                location.getBlockY() + "_" + location.getBlockZ();
        Location hologramLoc = location.clone().add(0.5, 1.5, 0.5);
        hologramLoc.setPitch(0);
        hologramLoc.setYaw(0);

        Hologram hologram = DHAPI.createHologram(hologramId, hologramLoc);
        if (hologram == null) return;

        updateHologramLines(hologram, location, quality);
        hologramIds.put(location, hologramId);
    }

    private void updateHologramLines(Hologram hologram, Location location, String quality) {
        String cropName = "None";
        Crop crop = null;
        for (int y = -3; y <= 0; y++) {
            Location below = location.clone().add(0, y, 0);
            crop = cropManager.getCrop(below);
            if (crop != null) {
                Drug drug = drugManager.getDrug(crop.getDrugId());
                cropName = drug != null ? drug.getName() : crop.getDrugId();
                break;
            }
        }
        List<String> lines = new ArrayList<>(Arrays.asList(
                MessageUtils.color("&#FFFF00&l" + quality + " Grow Light"),
                MessageUtils.color("&#D3D3D3Boosting: " + cropName)
        ));
        DHAPI.setHologramLines(hologram, lines);
    }

    public void updateHologram(Location location) {
        String hologramId = hologramIds.get(location);
        if (hologramId == null) return;
        Hologram hologram = DHAPI.getHologram(hologramId);
        if (hologram == null) return;
        String quality = growLights.get(location);
        updateHologramLines(hologram, location, quality);
    }

    public void removeGrowLight(Location location) {
        growLights.remove(location);
        String hologramId = hologramIds.remove(location);
        if (hologramId != null) {
            Hologram hologram = DHAPI.getHologram(hologramId);
            if (hologram != null) {
                hologram.delete();
            }
        }
    }

    public String getQualityAtLocation(Location location) {
        return growLights.get(location);
    }

    public boolean isGrowLightItem(ItemStack item) {
        if (item == null || item.getType() != Material.SEA_LANTERN || !item.hasItemMeta()) return false;
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains("Grow Light");
    }

    public String getQualityFromGrowLight(ItemStack item) {
        if (!isGrowLightItem(item)) return "Basic";
        String displayName = item.getItemMeta().getDisplayName();
        if (displayName.contains("Legendary")) return "Legendary";
        if (displayName.contains("Prime")) return "Prime";
        if (displayName.contains("Exotic")) return "Exotic";
        if (displayName.contains("Standard")) return "Standard";
        return "Basic";
    }

    private static final Map<String, String> qualityColors = new HashMap<>();

    static {
        qualityColors.put("Basic", "&#00FFFF");
        qualityColors.put("Standard", "&#00FF00");
        qualityColors.put("Exotic", "&#FF4500");
        qualityColors.put("Prime", "&#1E90FF");
        qualityColors.put("Legendary", "&#FFD700");
    }
}