package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.steeds.SteedManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final AddictionManager addictionManager;
    private final SteedManager vehicleManager;

    public PlayerInteractListener(DrugCraft plugin, DrugManager drugManager, AddictionManager addictionManager, SteedManager vehicleManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.addictionManager = addictionManager;
        this.vehicleManager = vehicleManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        String drugId = drugManager.getDrugIdFromItem(item);
        if (drugId != null) {
            addictionManager.applyDrugEffect(event.getPlayer(), drugId);
            item.setAmount(item.getAmount() - 1);
        } else if (item.getType() == Material.SADDLE && event.getPlayer().isSneaking()) {
            if (event.getAction().toString().contains("RIGHT")) {
                vehicleManager.summonSteed(event.getPlayer(), item);
            } else if (event.getAction().toString().contains("LEFT")) {
                vehicleManager.despawnSteed(event.getPlayer(), item);
            }
        }
    }
}