package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private final GrowLight growLight;

    public PlayerListener(DrugCraft plugin, CropManager cropManager, DrugManager drugManager, GrowLight growLight) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
        this.growLight = growLight;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Check if the clicked block is a grow light
        if (block.getType() == Material.SEA_LANTERN && growLight.getQualityAtLocation(block.getLocation()) != null) {
            event.setCancelled(true);
            block.setType(Material.AIR);
            String quality = growLight.getQualityAtLocation(block.getLocation());
            ItemStack growLightItem = growLight.createGrowLightItem(quality);
            block.getWorld().dropItemNaturally(block.getLocation(), growLightItem);
            growLight.removeGrowLight(block.getLocation());
            event.getPlayer().sendMessage(MessageUtils.color("&#FF7F00Removed " + quality + " Grow Light"));
            return;
        }

        if (block.getType() != Material.FARMLAND) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.WHEAT_SEEDS || !drugManager.isSeedItem(item)) return;

        Location cropLocation = block.getLocation().add(0, 1, 0);
        if (cropLocation.getBlock().getType() != Material.AIR) return;

        // Determine the drug ID and quality from the seed item
        String drugId = null;
        String quality = drugManager.getQualityFromItem(item);
        for (String id : drugManager.getSortedDrugs().stream().map(Drug::getDrugId).toList()) {
            ItemStack seedItem = drugManager.getSeedItem(id, quality);
            if (seedItem != null && seedItem.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName())) {
                drugId = id;
                break;
            }
        }

        if (drugId != null) {
            event.setCancelled(true);
            cropLocation.getBlock().setType(Material.WHEAT);
            Crop crop = new Crop(drugId, cropLocation, event.getPlayer().getUniqueId(), System.currentTimeMillis(), quality);
            cropManager.addCrop(crop);
            item.setAmount(item.getAmount() - 1);
            event.getPlayer().sendMessage(MessageUtils.color("&#FF7F00Planted " + drugId + " crop"));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.WHEAT) return;

        Crop crop = cropManager.getCrop(block.getLocation());
        if (crop == null) return;

        event.setCancelled(true);
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInHand == null || !isTrimmer(itemInHand)) {
            event.getPlayer().sendMessage(MessageUtils.color("&#FF4040You need trimmers to harvest this crop."));
            return;
        }

        double growth = cropManager.getGrowthPercentage(crop, "Basic");
        if (growth < 100) {
            event.getPlayer().sendMessage(MessageUtils.color("&#FF4040This crop is not fully grown yet."));
            return;
        }

        Drug drug = drugManager.getDrug(crop.getDrugId());
        if (drug != null) {
            ItemStack drugItem = drugManager.getDrugItem(crop.getDrugId(), crop.getQuality());
            if (drugItem != null) {
                int yield = cropManager.getYieldMultiplier(crop.getQuality());
                drugItem.setAmount(yield);
                block.getWorld().dropItemNaturally(block.getLocation(), drugItem);
                event.getPlayer().sendMessage(MessageUtils.color("&#FF7F00Harvested " + yield + " " + drug.getName()));
            }
            if (drug.hasSeed()) {
                ItemStack seedItem = drugManager.getSeedItem(crop.getDrugId(), crop.getQuality());
                if (seedItem != null) {
                    block.getWorld().dropItemNaturally(block.getLocation(), seedItem);
                }
            }
        }
        block.setType(Material.AIR);
        cropManager.removeCrop(crop);
    }

    private boolean isTrimmer(ItemStack item) {
        if (item == null || item.getType() != Material.SHEARS || !item.hasItemMeta()) return false;
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains("Trimmer");
    }
}