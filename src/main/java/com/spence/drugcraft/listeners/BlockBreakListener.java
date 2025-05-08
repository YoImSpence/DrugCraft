package com.spence.drugcraft.listeners;

import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
            if (entity instanceof ArmorStand armorStand && GrowLight.isGrowLight(armorStand)) {
                ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
                String quality = GrowLight.getQualityAtLocation(location);
                ItemStack growLightItem = GrowLight.createGrowLightItem(quality);
                if (itemInHand != null && itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    event.getBlock().getWorld().dropItemNaturally(location, growLightItem);
                    event.getPlayer().sendMessage(MessageUtils.color("&#00FF7FPicked up " + quality + " Grow Light with Silk Touch"));
                }
                GrowLight.removeGrowLight(location);
                break;
            }
        }
    }
}