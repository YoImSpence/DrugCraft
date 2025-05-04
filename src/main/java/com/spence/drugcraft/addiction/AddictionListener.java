package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AddictionListener implements Listener {
    private final DrugCraft plugin;
    private final AddictionManager addictionManager;

    public AddictionListener(DrugCraft plugin, AddictionManager addictionManager) {
        this.plugin = plugin;
        this.addictionManager = addictionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addictionManager.getPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        addictionManager.getPlayerData(event.getPlayer().getUniqueId());
        plugin.getDataManager().savePlayerData(event.getPlayer().getUniqueId(), addictionManager.getPlayerData(event.getPlayer().getUniqueId()));
    }
}