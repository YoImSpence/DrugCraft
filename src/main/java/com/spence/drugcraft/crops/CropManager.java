package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class CropManager {
    private final DrugCraft plugin;
    private final List<Crop> crops = new ArrayList<>();

    public CropManager(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void addCrop(Crop crop) {
        crops.add(crop);
    }

    public void removeCrop(Location location) {
        crops.removeIf(crop -> crop.getLocation().equals(location));
    }

    public Crop getCrop(Location location) {
        return crops.stream().filter(crop -> crop.getLocation().equals(location)).findFirst().orElse(null);
    }

    public void updateCrops() {
        for (Crop crop : new ArrayList<>(crops)) {
            Block block = crop.getLocation().getBlock();
            if (plugin.getGrowLight().isGrowLightBlock(block)) {
                crop.updateGrowth(0.1);
            }
        }
    }
}