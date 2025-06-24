package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public BlockPlaceListener(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != org.bukkit.Material.CHEST) return;

        org.bukkit.entity.Player player = event.getPlayer();
        String cartelId = cartelManager.getCartelByPlayer(player.getUniqueId()).getId();

        if (cartelId == null) return;

        if (!cartelManager.hasPermission(player.getUniqueId(), "build")) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "cartel.no-permission");
            return;
        }

        // Placeholder: Register new stash with CartelManager
        MessageUtils.sendMessage(player, "general.invalid-input"); // Notify stash placement
    }
}