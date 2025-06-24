package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.handlers.CasinoGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CasinoRegionListener implements Listener {
    private final DrugCraft plugin;

    public CasinoRegionListener(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        com.sk89q.worldedit.util.Location weLocation = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getLocation());

        boolean inCasinoRegion = query.getApplicableRegions(weLocation).getRegions().stream()
                .anyMatch(region -> region.getId().startsWith("casino"));
        if (!inCasinoRegion) {
            MessageUtils.sendMessage(player, "casino.not-in-casino");
            return;
        }

        plugin.getServer().getServicesManager()
                .getRegistration(CasinoGUIHandler.class).getProvider()
                .openMainMenu(player);
    }
}