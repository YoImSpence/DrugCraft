package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.vehicles.Steed;
import com.spence.drugcraft.vehicles.VehicleManager;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SteedListener implements Listener {
    private final DrugCraft plugin;
    private final VehicleManager vehicleManager;

    public SteedListener(DrugCraft plugin, VehicleManager vehicleManager) {
        this.plugin = plugin;
        this.vehicleManager = vehicleManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !vehicleManager.isSteedItem(item)) return;

        event.setCancelled(true);
        if (!player.hasPermission("drugcraft.vehicle")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return;
        }

        if (vehicleManager.canSummonSteed(player)) {
            Horse horse = vehicleManager.summonSteed(player, item);
            if (horse != null) {
                MessageUtils.sendMessage(player, "vehicle.summoned", "steed_name", horse.getCustomName());
            } else {
                MessageUtils.sendMessage(player, "vehicle.summon-failed");
            }
        } else {
            MessageUtils.sendMessage(player, "vehicle.already-summoned");
        }
    }
}