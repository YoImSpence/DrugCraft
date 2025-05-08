package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class CropListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private final Random random;

    public CropListener(DrugCraft plugin, CropManager cropManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
        this.random = new Random();
    }

    @EventHandler
    public void onCropHarvest(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.WHEAT) return;
        Location location = block.getLocation();
        Crop crop = cropManager.getCrop(location);
        if (crop == null) return;
        event.setDropItems(false);
        double growth = cropManager.getGrowthPercentage(crop, "Basic");
        if (growth < 100) {
            event.getPlayer().sendMessage(MessageUtils.color("&#FF4040This crop is not fully grown! " + String.format("%.2f", growth) + "%"));
            event.setCancelled(true);
            return;
        }
        String drugId = crop.getDrugId();
        String quality = crop.getQuality();
        ItemStack trimmer = event.getPlayer().getInventory().getItemInMainHand();
        String trimmerQuality = drugManager.getQualityFromItem(trimmer);
        if (trimmer.getType() == Material.SHEARS) {
            quality = switch (trimmerQuality) {
                case "Legendary" -> "Legendary";
                case "Prime" -> "Prime";
                case "Exotic" -> "Exotic";
                case "Standard" -> "Standard";
                default -> quality;
            };
        }
        ItemStack drugItem = drugManager.getDrugItem(drugId, quality);
        if (drugItem == null) {
            event.getPlayer().sendMessage(MessageUtils.color("&#FF4040Failed to harvest drug: " + drugId));
            return;
        }
        block.getWorld().dropItemNaturally(location, drugItem);
        int seedDropChance = plugin.getConfigManager().getConfig().getInt("crops.seed_drop_chance", 20);
        if (random.nextInt(100) < seedDropChance) {
            ItemStack seedItem = drugManager.getSeedItem(drugId, quality);
            if (seedItem != null) {
                block.getWorld().dropItemNaturally(location, seedItem);
            }
        }
        cropManager.removeCrop(crop);
        event.getPlayer().sendMessage(MessageUtils.color("�FF7FHarvested " + drugItem.getItemMeta().getDisplayName()));
    }
}