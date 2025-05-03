package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;

    public PlayerListener(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        // Handle drug usage
        if (drugManager.isDrugItem(item)) {
            drugManager.useDrug(event.getPlayer(), item);
            event.setCancelled(true);
        }
    }
}