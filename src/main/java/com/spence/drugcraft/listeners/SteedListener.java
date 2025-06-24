package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.*;
import com.spence.drugcraft.steeds.SteedManager;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class SteedListener implements Listener {
    private final DrugCraft plugin;
    private final SteedManager vehicleManager;

    public SteedListener(DrugCraft plugin, SteedManager vehicleManager) {
        this.plugin = plugin;
        this.vehicleManager = vehicleManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Horse horse) || !(event.getDamager() instanceof Player attacker)) return;
        if (vehicleManager.isFriendlyDamage(attacker, horse)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Horse horse) || horse.getOwner() == null) return;
        vehicleManager.onSteedDeath(horse, (Player) horse.getOwner());
    }
}