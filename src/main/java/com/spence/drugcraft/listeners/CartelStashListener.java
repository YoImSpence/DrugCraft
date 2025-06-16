package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CartelStashListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelStashListener(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        String cartelName = cartelManager.getCartelByStashLocation(location);
        if (cartelName == null) return;

        Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) return;

        boolean hasPermission = cartel.hasPermission(player.getUniqueId(), "manage_stash");
        if (!hasPermission) {
            MessageUtils.sendMessage(player, "cartel.no-permission", "permission", "manage_stash");
            event.setCancelled(true);
            plugin.getLogger().info("Player " + player.getName() + " attempted to access cartel stash at " + location + " without permission");
        }
    }
}