package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class CartelStashBreakListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelStashBreakListener(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Cartel cartel = cartelManager.getCartelByStashLocation(event.getBlock().getLocation());
        if (cartel == null) return;

        String cartelName = cartel.getName();
        Cartel playerCartel = cartelManager.getCartel(cartelName);
        if (playerCartel != null && !playerCartel.getRank(event.getPlayer().getUniqueId()).equals("leader")) {
            MessageUtils.sendMessage(event.getPlayer(), "cartel.no-permission");
            event.setCancelled(true);
        }
    }
}