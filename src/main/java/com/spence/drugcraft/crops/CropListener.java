package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;

public class CropListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private final PoliceManager policeManager;

    public CropListener(DrugCraft plugin, CropManager cropManager, DrugManager drugManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
        this.policeManager = policeManager;
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (!cropManager.isDrugCrop(block)) {
            return;
        }

        Location location = block.getLocation();
        event.setCancelled(true);
        plugin.getLogger().info("Cancelled vanilla growth for drug crop at " + location.toString() + "; CropManager will handle growth");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        if (!cropManager.isDrugCrop(block)) {
            plugin.getLogger().info("Block break at " + location.toString() + " is not a drug crop; ignoring");
            return;
        }

        CropManager.Crop crop = cropManager.getCrop(location);
        if (crop == null) {
            plugin.getLogger().warning("Drug crop at " + location.toString() + " has no associated crop data");
            return;
        }

        plugin.getLogger().info("Player " + player.getName() + " broke a drug crop at " + location.toString() + " (Drug ID: " + crop.getDrugId() + ", Quality: " + crop.getQuality() + ", Growth: " + cropManager.getCurrentGrowth(crop) + "%)");

        event.setDropItems(false);
        if (cropManager.getCurrentGrowth(crop) < 100.0) {
            if (!plugin.getConfig().getBoolean("crops.allow_break_immature", false)) {
                MessageUtils.sendMessage(player, "crop-listener.crop-not-mature");
                event.setCancelled(true);
                plugin.getLogger().info("Cancelled block break at " + location.toString() + "; Crop is not mature (Growth: " + cropManager.getCurrentGrowth(crop) + "%)");
                return;
            }
            double seedDropChance = plugin.getConfig().getDouble("crops.seed_drop_chance", 0.0);
            if (Math.random() < seedDropChance) {
                ItemStack seed = drugManager.getSeedItem(crop.getDrugId(), crop.getQuality(), player);
                if (seed != null) {
                    player.getInventory().addItem(seed);
                    plugin.getLogger().info("Dropped seed item for " + crop.getDrugId() + " (" + crop.getQuality() + ") to player " + player.getName() + " at " + location.toString());
                } else {
                    plugin.getLogger().warning("Failed to create seed item for " + crop.getDrugId() + " (" + crop.getQuality() + ") at " + location.toString());
                }
            } else {
                plugin.getLogger().info("No seed dropped for immature crop break at " + location.toString() + " (Chance: " + seedDropChance + ")");
            }
            cropManager.removeCrop(location);
            plugin.getLogger().info("Removed immature drug crop at " + location.toString());
        } else {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (!drugManager.isTrimmer(itemInHand)) {
                MessageUtils.sendMessage(player, "crop-listener.trimmer-required");
                event.setCancelled(true);
                plugin.getLogger().info("Cancelled block break at " + location.toString() + "; Player " + player.getName() + " does not have a trimmer");
                return;
            }
            ItemStack drugItem = drugManager.getDrugItem(crop.getDrugId(), crop.getQuality(), player);
            if (drugItem != null) {
                player.getInventory().addItem(drugItem);
                MessageUtils.sendMessage(player, "crop-listener.harvested", "drug_id", crop.getDrugId(), "quality", crop.getQuality());
                plugin.getLogger().info("Player " + player.getName() + " harvested " + crop.getDrugId() + " (" + crop.getQuality() + ") at " + location.toString());
            } else {
                plugin.getLogger().warning("Failed to create drug item for " + crop.getDrugId() + " (" + crop.getQuality() + ") at " + location.toString());
            }
            policeManager.notifyPolice(player, "Harvesting Illegal Crop");
            plugin.getLogger().info("Notified police of illegal crop harvesting by player " + player.getName() + " at " + location.toString());
            cropManager.removeCrop(location);
            plugin.getLogger().info("Removed mature drug crop at " + location.toString());
        }
    }
}