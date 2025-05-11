package com.spence.drugcraft.listeners;

import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakListener implements Listener {
    private final GrowLight growLight;

    public BlockBreakListener(GrowLight growLight) {
        this.growLight = growLight;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.SEA_LANTERN) return;

        Location location = event.getBlock().getLocation();
        String quality = growLight.getQualityAtLocation(location);
        if (quality == null) return; // Not a grow light

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        ItemStack growLightItem = growLight.createGrowLightItem(quality);
        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            event.getBlock().getWorld().dropItemNaturally(location, growLightItem);
            event.getPlayer().sendMessage(MessageUtils.color("&#FF7F00Picked up " + quality + " Grow Light with Silk Touch"));
        }
        growLight.removeGrowLight(location);
    }
}