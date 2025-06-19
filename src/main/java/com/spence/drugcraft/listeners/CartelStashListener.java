package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;

public class CartelStashListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelStashListener(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        Cartel cartel = cartelManager.getCartelByStashLocation(block.getLocation());
        if (cartel == null) return;

        String cartelName = cartel.getName();
        if (!cartel.hasPermission(event.getPlayer().getUniqueId(), "access_stash")) {
            MessageUtils.sendMessage(event.getPlayer(), "cartel.no-permission");
            event.setCancelled(true);
        } else {
            plugin.getCartelStash().openStash(event.getPlayer(), block);
        }
    }
}