package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private final GrowLight growLight;

    public BlockPlaceListener(DrugCraft plugin, CropManager cropManager, DrugManager drugManager, GrowLight growLight) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
        this.growLight = growLight;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();
        Location location = block.getLocation();

        // Check if the player can place drug-related blocks (e.g., grow lights, seeds)
        if (growLight.isGrowLightItem(item) || drugManager.isSeedItem(item)) {
            if (!plugin.getHouseManager().canUseDrugBlocks(player, location)) {
                player.sendMessage(MessageUtils.color("&#FF4040You can only place drug-related blocks in a house you own."));
                event.setCancelled(true);
                return;
            }
        }

        if (growLight.isGrowLightItem(item)) {
            event.setCancelled(true); // Prevent block placement
            String quality = growLight.getQualityFromGrowLight(item);
            if (growLight.placeGrowLight(location, quality)) {
                item.setAmount(item.getAmount() - 1); // Consume the item
                event.getPlayer().sendMessage(MessageUtils.color("&#FF7F00Placed " + quality + " Grow Light"));
                // Update nearby crops
                for (int y = -3; y <= 0; y++) {
                    Location below = location.clone().add(0, y, 0);
                    Block belowBlock = below.getBlock();
                    if (belowBlock.getType() == Material.WHEAT) {
                        Crop crop = cropManager.getCrop(below);
                        if (crop != null) {
                            cropManager.updateCropAge(crop);
                        }
                    }
                }
            } else {
                event.getPlayer().sendMessage(MessageUtils.color("&#FF4040Cannot place Grow Light here."));
            }
        } else if (item.getType() == Material.WHEAT_SEEDS && block.getType() == Material.WHEAT) {
            Crop crop = cropManager.getCrop(block.getLocation());
            if (crop != null) {
                event.setCancelled(true);
                return;
            }
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (x != 0 || z != 0) {
                        Block relative = block.getRelative(x, 0, z);
                        if (relative.getType() == Material.WHEAT) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
            // Determine the drug ID and quality from the seed item
            String drugId = null;
            String quality = drugManager.getQualityFromItem(item);
            for (String id : drugManager.getSortedDrugs().stream().map(d -> d.getDrugId()).toList()) {
                ItemStack seedItem = drugManager.getSeedItem(id, quality);
                if (seedItem != null && seedItem.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName())) {
                    drugId = id;
                    break;
                }
            }
            if (drugId != null) {
                event.setCancelled(true);
                Crop newCrop = new Crop(drugId, location, player.getUniqueId(), System.currentTimeMillis(), quality);
                cropManager.addCrop(newCrop);
                item.setAmount(item.getAmount() - 1);
                player.sendMessage(MessageUtils.color("&#FF7F00Planted " + drugId + " crop"));
            }
        }
    }
}