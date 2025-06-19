package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class CropListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final DataManager dataManager;
    private final PoliceManager policeManager;

    public CropListener(DrugCraft plugin, DrugManager drugManager, DataManager dataManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.dataManager = dataManager;
        this.policeManager = policeManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Crop crop = plugin.getCropManager().getCrop(event.getBlock().getLocation());

        if (crop == null) return;
        if (!drugManager.isTrimmer(item)) {
            event.setCancelled(true);
            return;
        }

        if (crop.isHarvestable()) {
            String quality = drugManager.getQualityFromItem(item);
            if (quality.compareTo(crop.getQuality()) >= 0) {
                ItemStack drugItem = drugManager.getDrugItem(crop.getStrain(), crop.getQuality(), player);
                player.getInventory().addItem(drugItem);
                crop.removeHologram();
                plugin.getCropManager().removeCrop(event.getBlock().getLocation());
                MessageUtils.sendMessage(player, "crops.trimmed");
                policeManager.notifyPolice(player, crop.getStrain());
            }
        } else {
            event.setCancelled(true);
        }
    }
}