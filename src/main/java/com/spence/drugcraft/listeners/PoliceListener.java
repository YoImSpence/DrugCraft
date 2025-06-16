package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.police.PoliceManager;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PoliceListener implements Listener {
    private final DrugCraft plugin;
    private final PoliceManager policeManager;
    private final Map<UUID, Long> recentDrugActions;

    public PoliceListener(DrugCraft plugin, PoliceManager policeManager) {
        this.plugin = plugin;
        this.policeManager = policeManager;
        this.recentDrugActions = new HashMap<>();
    }

    public void recordDrugAction(UUID playerId) {
        long currentTime = System.currentTimeMillis();
        recentDrugActions.put(playerId, currentTime);
        plugin.getLogger().info("Recorded drug action for player UUID " + playerId + " at time " + currentTime);
    }

    public Long getRecentDrugAction(UUID playerId) {
        Long lastAction = recentDrugActions.get(playerId);
        plugin.getLogger().info("Checked recent drug action for player UUID " + playerId + ": " + (lastAction != null ? lastAction : "none"));
        return lastAction;
    }

    public void clearDrugActions(UUID playerId) {
        recentDrugActions.remove(playerId);
        plugin.getLogger().info("Cleared drug actions for player UUID " + playerId);
    }
}