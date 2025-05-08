package com.spence.drugcraft.listeners;

import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick() && event.hasBlock() && event.getPlayer().isSneaking()) {
            Location location = event.getClickedBlock().getLocation();
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
                if (entity instanceof ArmorStand armorStand && GrowLight.isGrowLight(armorStand)) {
                    event.setCancelled(true);
                    String quality = GrowLight.getQualityAtLocation(location);
                    ItemStack growLightItem = GrowLight.createGrowLightItem(quality);
                    location.getWorld().dropItemNaturally(location, growLightItem);
                    GrowLight.removeGrowLight(location);
                    event.getPlayer().sendMessage(MessageUtils.color("&#00FF7FPicked up " + quality + " Grow Light"));
                    break;
                }
            }
        }
    }
}