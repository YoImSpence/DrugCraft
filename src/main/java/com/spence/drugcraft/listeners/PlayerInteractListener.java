package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.vehicles.VehicleManager;
import com.spence.drugcraft.crops.GrowLight;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;

public class PlayerInteractListener implements Listener {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final DrugManager drugManager;
    private final AddictionManager addictionManager;
    private final PoliceManager policeManager;
    private final GrowLight growLight;
    private final VehicleManager vehicleManager;

    public PlayerInteractListener(DrugCraft plugin, CropManager cropManager, DrugManager drugManager,
                                  AddictionManager addictionManager, PoliceManager policeManager,
                                  GrowLight growLight, VehicleManager vehicleManager) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.drugManager = drugManager;
        this.addictionManager = addictionManager;
        this.policeManager = policeManager;
        this.growLight = growLight;
        this.vehicleManager = vehicleManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // Example interaction handling
        if (vehicleManager.getPlayerSteed(player) != null) {
            // Handle steed interaction
        }
        if (event.isCancelled()) {
            vehicleManager.despawnSteed(player);
        }
    }
}