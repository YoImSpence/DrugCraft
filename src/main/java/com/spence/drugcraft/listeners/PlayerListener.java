package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final CropManager cropManager;
    private final DrugManager drugManager;

    public PlayerListener(DrugCraft plugin, DataManager dataManager, CropManager cropManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.FARMLAND) return;
        ItemStack item = event.getItem();
        if (item == null || !drugManager.isSeedItem(item)) return;

        String drugId = null;
        for (Drug drug : drugManager.getSortedDrugs()) {
            if (drug.hasSeed() && item.getType() == drug.getSeedItem(drug.getQuality()).getType() &&
                    item.getItemMeta().getDisplayName().equals(drug.getSeedItem(drug.getQuality()).getItemMeta().getDisplayName())) {
                drugId = drug.getDrugId();
                break;
            }
        }
        if (drugId == null) return;

        Location cropLocation = block.getLocation().add(0, 1, 0);
        if (cropLocation.getBlock().getType() != Material.AIR) return;

        Crop existingCrop = cropManager.getCrop(cropLocation);
        if (existingCrop != null) return;

        String quality = drugManager.getQualityFromItem(item);
        Crop crop = new Crop(drugId, cropLocation, System.currentTimeMillis(), quality);
        cropManager.addCrop(crop);
        cropLocation.getBlock().setType(Material.WHEAT);
        item.setAmount(item.getAmount() - 1);
        plugin.getLogger().info("Detected seed planting by " + event.getPlayer().getName() + " at " + cropLocation);
        event.getPlayer().sendMessage(MessageUtils.color("�FF7FPlanted " + drugId + " seed"));
        event.setCancelled(true);
    }
}