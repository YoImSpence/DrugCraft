package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.drugs.DrugManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class AddictionListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final AddictionManager addictionManager;

    public AddictionListener(DrugCraft plugin, DrugManager drugManager, AddictionManager addictionManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.addictionManager = addictionManager;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (drugManager.isDrugItem(item)) {
            String drugId = drugManager.getDrugIdFromItem(item);
            String quality = drugManager.getQualityFromItem(item);
            addictionManager.applyAddiction(event.getPlayer(), drugId, quality);
        }
    }
}