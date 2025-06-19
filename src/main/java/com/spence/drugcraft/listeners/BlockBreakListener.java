package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final CropManager cropManager;
    private final GrowLight growLight;
    private final DataManager dataManager;
    private final PoliceManager policeManager;

    public BlockBreakListener(DrugCraft plugin, DrugManager drugManager, CropManager cropManager, GrowLight growLight, DataManager dataManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.cropManager = cropManager;
        this.growLight = growLight;
        this.dataManager = dataManager;
        this.policeManager = policeManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (growLight.isGrowLightBlock(event.getBlock())) {
            event.setDropItems(false);
            String quality = growLight.getQualityFromBlock(event.getBlock());
            ItemStack lightItem = growLight.createGrowLight(quality);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), lightItem);
            policeManager.notifyPolice(event.getPlayer(), "grow_light");
            return;
        }

        Crop crop = cropManager.getCrop(event.getBlock().getLocation());
        if (crop != null) {
            event.setCancelled(true);
        }
    }
}