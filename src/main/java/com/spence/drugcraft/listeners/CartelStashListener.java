package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CartelStashListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelStashListener(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != org.bukkit.Material.CHEST) return;

        org.bukkit.entity.Player player = event.getPlayer();
        org.bukkit.Location location = event.getClickedBlock().getLocation();
        String cartelId = cartelManager.getCartelIdByStashLocation(location);

        if (cartelId == null) return;

        event.setCancelled(true);
        if (!cartelManager.isPlayerInCartel(player.getUniqueId(), cartelId)) {
            MessageUtils.sendMessage(player, "cartel.not-in-cartel");
            return;
        }

        if (!cartelManager.hasPermission(player.getUniqueId(), "interact")) {
            MessageUtils.sendMessage(player, "cartel.no-permission");
            return;
        }

        player.openInventory(((Container) event.getClickedBlock().getState()).getInventory());
    }
}