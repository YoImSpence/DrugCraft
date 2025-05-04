package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CropListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;

    public CropListener(DrugCraft plugin, CropManager cropManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.FARMLAND) return;
        ItemStack item = event.getItem();
        if (item == null || !drugManager.isSeedItem(item)) return;

        String drugId = drugManager.getDrugIdFromSeed(item);
        if (drugId == null) return;

        Block cropBlock = block.getRelative(0, 1, 0);
        if (cropBlock.getType() != Material.AIR) return;

        cropBlock.setType(Material.WHEAT);
        Crop crop = new Crop(cropBlock.getLocation(), drugId, System.currentTimeMillis());
        cropManager.addCrop(crop);

        item.setAmount(item.getAmount() - 1);
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.WHEAT) return;
        Crop crop = cropManager.getCrop(block.getLocation());
        if (crop == null) return;

        double growth = cropManager.getGrowthPercentage(crop);
        if (growth < 100) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "&cThe crop is not fully grown yet.");
            return;
        }

        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (tool.getType() != Material.SHEARS) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "&cYou need shears to harvest this crop.");
            return;
        }

        Drug drug = drugManager.getDrug(crop.getDrugId());
        if (drug != null) {
            ItemStack drugItem = drug.getItem();
            block.getWorld().dropItemNaturally(block.getLocation(), drugItem);
        }

        cropManager.removeCrop(crop);
        event.setDropItems(false);
    }
}