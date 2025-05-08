package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.Crop;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;

    public BlockPlaceListener(DrugCraft plugin, CropManager cropManager) {
        this.plugin = plugin;
        this.cropManager = cropManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();
        Location location = block.getLocation();

        if (GrowLight.isGrowLightItem(item)) {
            event.setCancelled(true); // Prevent block placement
            String quality = GrowLight.getQualityFromGrowLight(item);
            GrowLight.placeGrowLight(location, quality);
            item.setAmount(item.getAmount() - 1); // Consume the item
            event.getPlayer().sendMessage(MessageUtils.color("&#00FF7FPlaced " + quality + " Grow Light"));
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
        }
    }
}