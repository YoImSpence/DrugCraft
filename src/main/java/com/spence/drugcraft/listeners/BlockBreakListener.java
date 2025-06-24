package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;

    public BlockBreakListener(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Placeholder: Check if block is a crop
        Crop crop = null; // Retrieve crop from tracking system
        if (crop == null) return;

        org.bukkit.entity.Player player = event.getPlayer();
        if (!crop.isHarvestable()) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "crops.growing", "drug_id", crop.getDrugId().replace("_", " "), "progress", String.valueOf(crop.getGrowthProgress()));
            return;
        }

        event.setDropItems(false);
        org.bukkit.inventory.ItemStack drugItem = drugManager.createDrugItem(crop.getDrugId());
        if (drugItem != null) {
            player.getInventory().addItem(drugItem);
            MessageUtils.sendMessage(player, "crops.harvestable", "drug_id", crop.getDrugId().replace("_", " "), "quality", "Standard");
        }
        // Placeholder: Remove crop from tracking system
    }
}