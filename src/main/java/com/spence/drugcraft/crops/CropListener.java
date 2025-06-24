package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class CropListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;

    public CropListener(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String drugId = drugManager.getDrugIdFromItem(item);
        if (drugId == null) return;

        Drug drug = drugManager.getDrug(drugId);
        if (drug != null && drug.isGrowable()) {
            Block block = event.getBlockPlaced();
            block.setType(Material.WHEAT); // Placeholder crop
            MessageUtils.sendMessage(event.getPlayer(), "crops.planted", "drug_id", drugId, "quality", "Standard");
        } else {
            MessageUtils.sendMessage(event.getPlayer(), "crops.invalid-seed");
            event.setCancelled(true);
        }
    }
}