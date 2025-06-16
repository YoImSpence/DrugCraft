package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class CartelStashBreakListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelStashBreakListener(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        String cartelName = cartelManager.getCartelByStashLocation(location);
        if (cartelName == null) return;

        UUID playerUUID = player.getUniqueId();
        Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) return;
        if (!cartel.isLeader(playerUUID)) {
            MessageUtils.sendMessage(player, "cartel.leader-only", "action", "break the stash");
            event.setCancelled(true);
            plugin.getLogger().info("Player " + player.getName() + " attempted to break cartel stash at " + location + " but is not the leader");
            return;
        }
        if (!cartel.hasPermission(playerUUID, "manage_stash")) {
            MessageUtils.sendMessage(player, "cartel.no-permission", "permission", "manage_stash");
            event.setCancelled(true);
            plugin.getLogger().info("Player " + player.getName() + " attempted to break cartel stash at " + location + " without permission");
            return;
        }
        cartelManager.removeStashLocation(cartelName);
        MessageUtils.sendMessage(player, "cartel.stash-removed", "cartel_name", cartelName);
        plugin.getLogger().info("Player " + player.getName() + " removed cartel stash for " + cartelName + " at " + location);
    }
}