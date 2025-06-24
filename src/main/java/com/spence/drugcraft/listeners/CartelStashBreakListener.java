package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
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
        if (event.getBlock().getType() != org.bukkit.Material.CHEST) return;

        org.bukkit.entity.Player player = event.getPlayer();
        org.bukkit.Location location = event.getBlock().getLocation();
        String cartelId = cartelManager.getCartelIdByStashLocation(location);

        if (cartelId == null) return;

        event.setCancelled(true);
        if (!cartelManager.isPlayerInCartel(player.getUniqueId(), cartelId)) {
            MessageUtils.sendMessage(player, "cartel.not-in-cartel");
            return;
        }

        if (!cartelManager.hasPermission(player.getUniqueId(), "build")) {
            MessageUtils.sendMessage(player, "cartel.no-permission");
            return;
        }

        // Placeholder: Remove stash from cartel
        MessageUtils.sendMessage(player, "cartel.stash-removed");
    }
}