package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.logging.Logger;

public class CropListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private final Logger logger;
    private final Random random = new Random();

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
                    event.getPlayer().sendMessage(MessageUtils.color("{#FF5555}Crops can only be planted on farmland!"));
                    event.setCancelled(true);
                    return;
                }
                Location cropLoc = new Location(
                        farmlandBlock.getWorld(),
                        farmlandBlock.getX(),
                        farmlandBlock.getY() + 1,
                        farmlandBlock.getZ()
                );
                cropLoc.setPitch(0);
                cropLoc.setYaw(0);
                if (cropManager.getCrop(cropLoc) != null) {
                    event.getPlayer().sendMessage(MessageUtils.color("{#FF5555}A crop is already planted here!"));
                    event.setCancelled(true);
                    logger.fine("Prevented duplicate crop planting at " + cropLoc);
                    return;
                }
                Block cropBlock = cropLoc.getBlock();
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x != 0 || z != 0) {
                            cropBlock.getRelative(x, 1, z).setType(Material.AIR);
                            Block adjacent = cropBlock.getRelative(x, 0, z);
                            if (adjacent.getType() == Material.WHEAT && cropManager.getCrop(adjacent.getLocation()) == null) {
                                logger.fine("Clearing adjacent wheat block at " + adjacent.getLocation());
                                adjacent.setType(Material.AIR);
                            }
                        }
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        cropBlock.setType(Material.AIR);
                        cropBlock.setType(Material.WHEAT);
                        cropBlock.getState().update(true, true);
                        logger.fine("Set crop block to wheat at " + cropLoc);
                    }
                }.runTask(plugin);
                Crop crop = new Crop(cropLoc, drugId, System.currentTimeMillis());
                cropManager.addCrop(crop);
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    event.getPlayer().getInventory().setItemInMainHand(null);
                }
                event.setCancelled(true);
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
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE || !event.isCancelled()) {
                logger.fine("Allowing wheat block break at " + block.getLocation() + " in creative mode or via command");
                return;
            }
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
            event.setCancelled(true);
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
                String quality = drugManager.getQualityFromItem(tool);
                event.getPlayer().getInventory().addItem(drug.getSeedItem(quality));
                logger.info("Recovered seed for crop: " + crop.getDrugId() + " at " + crop.getLocation().toString() + " using Silk Touch");
                cropManager.removeCrop(crop);
            } else {
                logger.warning("Drug not found or no seed for crop ID: " + crop.getDrugId() + " during Silk Touch recovery");
                event.setCancelled(true);
                event.getPlayer().sendMessage(MessageUtils.color("{#FF5555}Cannot recover seed for this crop!"));
            }
            return;
        }

        if (growth >= 100) {
            if (tool != null && (tool.getType() == Material.SHEARS || isTrimmer(tool))) {
                Drug drug = drugManager.getDrug(crop.getDrugId());
                if (drug != null) {
                    event.setDropItems(false);
                    String quality = getHarvestQuality(tool);
                    int yield = getHarvestYield(tool);
                    ItemStack harvestedItem = drug.getItem(quality);
                    harvestedItem.setAmount(yield);
                    event.getPlayer().getInventory().addItem(harvestedItem);
                    logger.info("Harvested crop: " + crop.getDrugId() + " at " + crop.getLocation().toString() + " with quality " + quality + " and yield " + yield);
                } else {
                    logger.warning("Drug not found for crop ID: " + crop.getDrugId() + " during harvest");
                }
                cropManager.removeCrop(crop);
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(MessageUtils.color("{#FF5555}You need shears or a trimmer to harvest this crop!"));
                logger.info("Player " + event.getPlayer().getName() + " attempted to harvest " + crop.getDrugId() + " without shears/trimmer");
            }
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtils.color("{#FF5555}This crop is not fully grown!"));
            logger.info("Player " + event.getPlayer().getName() + " attempted to break immature crop: " + crop.getDrugId() + " at " + crop.getLocation().toString());
        }
    }

    private boolean isTrimmer(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = tool.getItemMeta();
        return meta.hasLore() && meta.getLore().stream().anyMatch(line -> line.contains("Quality: "));
    }

    private String getHarvestQuality(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta()) {
            return "Basic";
        }
        ItemMeta meta = tool.getItemMeta();
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.contains("Quality: Legendary")) {
                    return random.nextDouble() < 0.7 ? "Legendary" : "Prime";
                } else if (line.contains("Quality: Prime")) {
                    return random.nextDouble() < 0.6 ? "Prime" : "Exotic";
                } else if (line.contains("Quality: Exotic")) {
                    return random.nextDouble() < 0.5 ? "Exotic" : "Standard";
                } else if (line.contains("Quality: Standard")) {
                    return random.nextDouble() < 0.5 ? "Standard" : "Basic";
                } else if (line.contains("Quality: Basic")) {
                    return "Basic";
                }
            }
        }
        return "Basic";
    }

    private int getHarvestYield(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta()) {
            return 1;
        }
        ItemMeta meta = tool.getItemMeta();
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.contains("Quality: Legendary")) {
                    return 3 + random.nextInt(2); // 3-4 items
                } else if (line.contains("Quality: Prime")) {
                    return 2 + random.nextInt(2); // 2-3 items
                } else if (line.contains("Quality: Exotic")) {
                    return 2; // 2 items
                } else if (line.contains("Quality: Standard")) {
                    return 1 + random.nextInt(1); // 1-2 items
                } else if (line.contains("Quality: Basic")) {
                    return 1;
                }
            }
        }
        return 1;
    }
}