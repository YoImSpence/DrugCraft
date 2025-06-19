package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final CropManager cropManager;
    private final GrowLight growLight;
    private final DataManager dataManager;

    public BlockPlaceListener(DrugCraft plugin, DrugManager drugManager, CropManager cropManager, GrowLight growLight, DataManager dataManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.cropManager = cropManager;
        this.growLight = growLight;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlock();
        if (block.getType() != Material.FARMLAND) return;

        if (drugManager.isSeedItem(item)) {
            String drugId = drugManager.getDrugIdFromItem(item);
            String quality = drugManager.getQualityFromItem(item);
            int requiredLevel = plugin.getConfig("drugs.yml").getInt("drugs." + drugId + ".level", 0);
            if (dataManager.getPlayerLevel(event.getPlayer().getUniqueId()) < requiredLevel) {
                MessageUtils.sendMessage(event.getPlayer(), "crops.level-required", "level", String.valueOf(requiredLevel));
                event.setCancelled(true);
                return;
            }
            Crop crop = new Crop(plugin, drugId, block.getLocation(), quality);
            cropManager.addCrop(crop);
            block.setType(Material.WHEAT);
            item.setAmount(item.getAmount() - 1);
            MessageUtils.sendMessage(event.getPlayer(), "crops.planted");
        } else if (growLight.isGrowLightItem(item)) {
            // Placeholder: Implement grow light placement
        }
    }
}