package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.casino.CasinoManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.CasinoGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class CasinoGameListener implements Listener {
    private final DrugCraft plugin;
    private final CasinoManager casinoManager;

    public CasinoGameListener(DrugCraft plugin, CasinoManager casinoManager) {
        this.plugin = plugin;
        this.casinoManager = casinoManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !List.of("BACCARAT", "BLACKJACK", "POKER", "ROULETTE", "SLOTS").contains(activeGUI.getType())) return;

        if (casinoManager.getActiveGame(player.getUniqueId()) != null && !casinoManager.getActiveGame(player.getUniqueId()).isGameOver()) {
            // Re-open GUI to prevent premature closure
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                new CasinoGUI(plugin, casinoManager).openGameMenu(player, activeGUI.getType(), casinoManager.getActiveGame(player.getUniqueId()));
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (casinoManager.getActiveGame(player.getUniqueId()) != null) {
            casinoManager.endGame(player.getUniqueId());
        }
        plugin.getActiveMenus().remove(player.getUniqueId());
    }
}