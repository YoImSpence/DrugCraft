package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class CropManager {
    private final DrugCraft plugin;
    private final Map<Location, Crop> crops = new HashMap<>();

    public CropManager(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void plantCrop(String drugId, Location location, String quality) {
        Crop crop = new Crop(drugId, location);
        crops.put(location, crop);
        // Placeholder: Create hologram using DecentHolograms
    }

    public Crop getCrop(Location location) {
        return crops.get(location);
    }

    public void removeCrop(Location location) {
        Crop crop = crops.remove(location);
        if (crop != null) {
            crop.removeHologram();
        }
    }
}