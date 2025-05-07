package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AddictionListener implements Listener {
    private final DrugCraft plugin;
    private final AddictionManager addictionManager;

    public AddictionListener(DrugCraft plugin, AddictionManager addictionManager) {
        this.plugin = plugin;
        this.addictionManager = addictionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addictionManager.loadPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        addictionManager.savePlayerData(event.getPlayer());
        addictionManager.clearPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction().isRightClick() &&
                plugin.getDrugManager().isDrugItem(event.getItem())) {
            String drugId = plugin.getDrugManager().getDrugIdFromItem(event.getItem());
            if (drugId != null) {
                addictionManager.incrementDrugUse(event.getPlayer(), drugId);
            }
        }
    }
}