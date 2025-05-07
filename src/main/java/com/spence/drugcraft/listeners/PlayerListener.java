package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final CartelManager cartelManager;

    public PlayerListener(DrugCraft plugin, DrugManager drugManager, CartelManager cartelManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.cartelManager = cartelManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction().isRightClick() && drugManager.isDrugItem(event.getItem())) {
            if (!plugin.getPermissionManager().hasPermission(event.getPlayer(), "drugcraft.use")) {
                event.getPlayer().sendMessage(MessageUtils.color("&cYou do not have permission to use drugs."));
                event.setCancelled(true);
                return;
            }
            drugManager.useDrug(event.getPlayer(), event.getItem());
        }
    }
}