package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakListener implements Listener {
    private final DrugCraft plugin;
    private final GrowLight growLight;
    private final CropManager cropManager;
    private final PoliceManager policeManager;

    public BlockBreakListener(DrugCraft plugin, GrowLight growLight, CropManager cropManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.growLight = growLight;
        this.cropManager = cropManager;
        this.policeManager = policeManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        plugin.getLogger().info("BlockBreakEvent triggered for player " + event.getPlayer().getName() + " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());

        // Handle Grow Light breaking
        if (growLight.isGrowLightBlock(block)) {
            plugin.getLogger().info("Block identified as Grow Light at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
            if (tool == null || !tool.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                plugin.getLogger().info("Player " + event.getPlayer().getName() + " broke Grow Light without Silk Touch at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                MessageUtils.sendMessage(event.getPlayer(), "block-break.silk-touch-required");
                String quality = growLight.getQualityFromBlock(block);
                MessageUtils.sendMessage(event.getPlayer(), "block-break.grow-light-broken", "quality", quality != null ? quality : "Unknown");
                event.setDropItems(false);
                policeManager.notifyPolice(event.getPlayer(), "Breaking Grow Light"); // Removed extra Location parameter
                plugin.getLogger().info("Notified police of Grow Light break by player " + event.getPlayer().getName() + " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                // Force a block update to clear any lingering data
                block.setType(Material.AIR);
                NBTBlock nbtBlock = new NBTBlock(block);
                nbtBlock.getData().setString("drugcraft_type", null);
                nbtBlock.getData().setString("quality", null);
                block.getState().update(true, true);
                plugin.getLogger().info("Cleared Grow Light data and forced block update at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                return;
            }
            // If Silk Touch is used, the PlayerInteractListener will handle pickup on right-click
        }

        // Handle drug crop breaking (already handled by CropListener, but log for consistency)
        if (cropManager.isDrugCrop(block)) {
            plugin.getLogger().info("Block identified as drug crop at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "; Handled by CropListener");
        }
    }
}