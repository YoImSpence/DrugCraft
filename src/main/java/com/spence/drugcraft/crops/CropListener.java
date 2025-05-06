package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import java.util.logging.Logger;

public class CropListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private final Logger logger;

    public CropListener(DrugCraft plugin, CropManager cropManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (drugManager.isSeedItem(item)) {
            String drugId = drugManager.getDrugIdFromSeed(item);
            if (drugId != null) {
                Block farmlandBlock = event.getBlockPlaced().getRelative(0, -1, 0);
                if (farmlandBlock.getType() != Material.FARMLAND) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Crops can only be planted on farmland!");
                    event.setCancelled(true);
                    return;
                }
                // Use exact farmland Y+1 coordinates
                Location cropLoc = new Location(
                        farmlandBlock.getWorld(),
                        farmlandBlock.getX(),
                        farmlandBlock.getY() + 1,
                        farmlandBlock.getZ()
                );
                cropLoc.setPitch(0);
                cropLoc.setYaw(0);
                // Check for existing crop
                if (cropManager.getCrop(cropLoc) != null) {
                    event.getPlayer().sendMessage(ChatColor.RED + "A crop is already planted here!");
                    event.setCancelled(true);
                    logger.fine("Prevented duplicate crop planting at " + cropLoc);
                    return;
                }
                Block cropBlock = cropLoc.getBlock();
                // Clear 5x5 area around crop (Y+1 and Y+2), excluding target crop
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x != 0 || z != 0) { // Skip target crop position
                            cropBlock.getRelative(x, 1, z).setType(Material.AIR);
                            Block adjacent = cropBlock.getRelative(x, 0, z);
                            if (adjacent.getType() == Material.WHEAT && cropManager.getCrop(adjacent.getLocation()) == null) {
                                logger.fine("Clearing adjacent wheat block at " + adjacent.getLocation());
                                adjacent.setType(Material.AIR);
                            }
                        }
                    }
                }
                // Schedule block placement to ensure sync
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        cropBlock.setType(Material.AIR);
                        cropBlock.setType(Material.WHEAT);
                        cropBlock.getState().update(true, true); // Force client update
                        logger.fine("Set crop block to wheat at " + cropLoc);
                    }
                }.runTask(plugin);
                Crop crop = new Crop(cropLoc, drugId, System.currentTimeMillis());
                cropManager.addCrop(crop);
                event.setCancelled(true); // Prevent vanilla crop placement
                logger.info("Planted crop: " + drugId + " at " + cropLoc.toString());
            } else {
                logger.warning("Invalid drug ID for seed item at " + event.getBlockPlaced().getLocation());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.WHEAT) {
            logger.fine("Block at " + block.getLocation() + " is not wheat, skipping break event");
            return;
        }
        Crop crop = cropManager.getCrop(block.getLocation());
        if (crop == null) {
            // Allow breaking in creative mode or via commands
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE || !event.isCancelled()) {
                logger.fine("Allowing wheat block break at " + block.getLocation() + " in creative mode or via command");
                return;
            }
            // Check if block is in 3x3 area around a registered crop
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Crop nearbyCrop = cropManager.getCrop(block.getRelative(x, 0, z).getLocation());
                    if (nearbyCrop != null) {
                        logger.fine("Preventing break of wheat block near crop at " + block.getLocation());
                        event.setDropItems(false);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            logger.fine("No crop found at " + block.getLocation() + ", preventing vanilla wheat drops");
            event.setDropItems(false);
            event.setCancelled(true); // Prevent vanilla wheat behavior
            return;
        }

        double growth = cropManager.getGrowthPercentage(crop);
        logger.info("Attempting to break crop: " + crop.getDrugId() + " at " + crop.getLocation() + " with growth " + String.format("%.2f", growth) + "%");
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        boolean hasSilkTouch = tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH);

        if (hasSilkTouch) {
            Drug drug = drugManager.getDrug(crop.getDrugId());
            if (drug != null && drug.hasSeed()) {
                event.setDropItems(false);
                event.getPlayer().getInventory().addItem(drug.getSeedItem());
                logger.info("Recovered seed for crop: " + crop.getDrugId() + " at " + crop.getLocation().toString() + " using Silk Touch");
                cropManager.removeCrop(crop);
            } else {
                logger.warning("Drug not found or no seed for crop ID: " + crop.getDrugId() + " during Silk Touch recovery");
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Cannot recover seed for this crop!");
            }
            return;
        }

        if (growth >= 100) {
            if (tool != null && tool.getType() == Material.SHEARS) {
                Drug drug = drugManager.getDrug(crop.getDrugId());
                if (drug != null) {
                    event.setDropItems(false);
                    event.getPlayer().getInventory().addItem(drug.getItem());
                    logger.info("Harvested crop: " + crop.getDrugId() + " at " + crop.getLocation().toString());
                } else {
                    logger.warning("Drug not found for crop ID: " + crop.getDrugId() + " during harvest");
                }
                cropManager.removeCrop(crop);
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You need shears to harvest this crop!");
                logger.info("Player " + event.getPlayer().getName() + " attempted to harvest " + crop.getDrugId() + " without shears");
            }
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "This crop is not fully grown!");
            logger.info("Player " + event.getPlayer().getName() + " attempted to break immature crop: " + crop.getDrugId() + " at " + crop.getLocation().toString());
        }
    }
}