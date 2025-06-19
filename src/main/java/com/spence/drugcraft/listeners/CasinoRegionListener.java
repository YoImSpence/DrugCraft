package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;

public class CasinoRegionListener implements Listener {
    private final DrugCraft plugin;

    public CasinoRegionListener(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        Location worldEditLocation = BukkitAdapter.adapt(player.getLocation());
        com.sk89q.worldguard.protection.ApplicableRegionSet regions = query.getApplicableRegions(worldEditLocation);

        boolean inCasino = false;
        String gameRegion = null;

        for (com.sk89q.worldguard.protection.regions.ProtectedRegion region : regions) {
            String id = region.getId();
            if (id.equalsIgnoreCase("Casino")) {
                inCasino = true;
            } else if (id.matches("^(slots|bjack|rlet|poker|bacrat)\\d+$")) {
                gameRegion = id;
            }
        }

        if (!inCasino) {
            if (gameRegion != null) {
                MessageUtils.sendMessage(player, "casino.not-in-casino");
                event.setCancelled(true);
            }
            return;
        }

        if (gameRegion == null) return;

        event.setCancelled(true);
        if (gameRegion.startsWith("slots")) {
            plugin.getCasinoGUIHandler().openSlotsMenu(player);
        } else if (gameRegion.startsWith("bjack")) {
            plugin.getCasinoGUIHandler().openBlackjackMenu(player);
        } else if (gameRegion.startsWith("rlet")) {
            plugin.getCasinoGUIHandler().openRouletteMenu(player);
        } else if (gameRegion.startsWith("poker")) {
            plugin.getCasinoGUIHandler().openPokerMenu(player);
        } else if (gameRegion.startsWith("bacrat")) {
            plugin.getCasinoGUIHandler().openBaccaratMenu(player);
        }
    }
}