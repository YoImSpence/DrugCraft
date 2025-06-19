package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.heists.HeistManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class HeistListener implements Listener {
    private final DrugCraft plugin;
    private final HeistManager heistManager;

    public HeistListener(DrugCraft plugin, HeistManager heistManager) {
        this.plugin = plugin;
        this.heistManager = heistManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Placeholder: Implement heist interaction logic
    }
}