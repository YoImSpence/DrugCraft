package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.vehicles.Steed;
import com.spence.drugcraft.vehicles.VehicleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
    private final DrugCraft plugin;
    private final VehicleManager vehicleManager;

    public PlayerInteractListener(DrugCraft plugin, VehicleManager vehicleManager) {
        this.plugin = plugin;
        this.vehicleManager = vehicleManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("drugcraft.vehicle")) {
            return;
        }

        Steed steed = vehicleManager.getPlayerSteed(player);
        if (steed != null) {
            // Placeholder: Implement steed interaction logic
            MessageUtils.sendMessage(player, "vehicle.interacted");
        }
    }
}